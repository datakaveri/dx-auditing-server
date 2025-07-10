package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/url"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
	"github.com/codenotary/immudb/pkg/client"
)

var retryCounter = make(map[string]int)

func connectRabbitMQOrExit(config *Config) *amqp.Connection {
	amqpURI := fmt.Sprintf("amqp://%s:%s@%s:%d/%s",
		url.QueryEscape(config.DataBrokerUserName),
		url.QueryEscape(config.DataBrokerPassword),
		config.DataBrokerIP,
		config.DataBrokerPort,
		url.PathEscape(config.VirtualHost),
	)

	conn, err := amqp.DialConfig(amqpURI, amqp.Config{
		Heartbeat: time.Duration(config.RequestedHeartbeat) * time.Second,
		Dial:      amqp.DefaultDial(time.Duration(config.ConnectionTimeout) * time.Millisecond),
	})
	if err != nil {
		log.Fatalf("Failed to connect to RabbitMQ: %v", err)
	}
	log.Println("✅ Connected to RabbitMQ")
	return conn
}

func processMessages(ctx context.Context, conn *amqp.Connection, immudbClient client.ImmuClient, queueName string) {
	ch, err := conn.Channel()
	if err != nil {
		log.Fatalf("Failed to open channel: %v", err)
	}
	defer ch.Close()

	msgs, err := ch.Consume(
		queueName,
		"",
		false,
		false,
		false,
		false,
		nil,
	)
	if err != nil {
		log.Fatalf("Failed to register consumer: %v", err)
	}

	log.Printf("🚀 Started consuming from queue '%s'", queueName)

	for {
		select {
		case <-ctx.Done():
			log.Println("🛑 Graceful shutdown initiated")
			return
		case msg, ok := <-msgs:
			if !ok {
				log.Println("Channel closed")
				return
			}
			handleMessage(ctx, msg, immudbClient)
		}
	}
}

func handleMessage(ctx context.Context, msg amqp.Delivery, immudbClient client.ImmuClient) {
	var activity ActivityLog
	if err := json.Unmarshal(msg.Body, &activity); err != nil {
		log.Printf("⚠️ Failed to parse message: %v", err)
		if ackErr := msg.Ack(false); ackErr != nil {
			log.Printf("⚠️ Failed to acknowledge parse error: %v", ackErr)
		}
		return
	}

	// ✅ Validation: if any required fields are empty, skip insert but acknowledge to remove from queue
	if activity.ID == "" ||
		activity.AssetName == "" ||
		activity.AssetType == "" ||
		activity.Operation == "" ||
		activity.CreatedAt == "" ||
		activity.AssetID == "" ||
		activity.API == "" ||
		activity.Method == "" ||
		activity.Role == "" ||
		activity.UserID == "" ||
		activity.ShortDescription == "" ||
		activity.OriginServer == "" {
		log.Printf("🚫 Missing required non-empty fields, acknowledging to skip: %+v", activity)
		if ackErr := msg.Ack(false); ackErr != nil {
			log.Printf("⚠️ Failed to acknowledge after validation skip: %v", ackErr)
		}
		return
	}

	err := insertActivityLog(ctx, immudbClient, activity)
	if err != nil {
		if err.Error() == "duplicate" {
			log.Printf("🚫 Duplicate key, acknowledging message.")
			if ackErr := msg.Ack(false); ackErr != nil {
				log.Printf("⚠️ Failed to ack duplicate: %v", ackErr)
			}
		} else {
			retryCounter[activity.ID]++
			if retryCounter[activity.ID] < 6 {
				log.Printf("⚠️ Insert failed, attempt %d. Retrying after 5s...", retryCounter[activity.ID])
				time.Sleep(5 * time.Second)
				if nackErr := msg.Nack(false, true); nackErr != nil {
					log.Printf("⚠️ Failed to Nack for retry: %v", nackErr)
				}
			} else {
				log.Printf("🚀 Failed after 5 retries. Sending to DLX.")
				if nackErr := msg.Nack(false, false); nackErr != nil {
					log.Printf("⚠️ Failed to Nack to DLX: %v", nackErr)
				}
				delete(retryCounter, activity.ID)
			}
		}
		return
	}

	if ackErr := msg.Ack(false); ackErr != nil {
		log.Printf("⚠️ Failed to acknowledge message: %v", ackErr)
	}
}
