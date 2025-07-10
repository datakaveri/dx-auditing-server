package main

import (
	"context"
	"encoding/json"
	"log"
	"os"
	"os/signal"
	"syscall"
)

type Config struct {
	DataBrokerIP            string `json:"dataBrokerIP"`
	DataBrokerPort          int    `json:"dataBrokerPort"`
	DataBrokerUserName      string `json:"dataBrokerUserName"`
	DataBrokerPassword      string `json:"dataBrokerPassword"`
	ConnectionTimeout       int    `json:"connectionTimeout"`
	RequestedHeartbeat      int    `json:"requestedHeartbeat"`
	VirtualHost             string `json:"virtualHost"`
	QueueName               string `json:"queueName"`
	ImmuDatabaseIP       string `json:"immuDatabaseIP"`
	ImmuDatabasePort     int    `json:"immuDatabasePort"`
	ImmuDatabaseName     string `json:"immuDatabaseName"`
	ImmuDatabaseUserName string `json:"immuDatabaseUserName"`
	ImmuDatabasePassword string `json:"immuDatabasePassword"`
	ImmuPoolSize         int    `json:"immuPoolSize"`
}

func main() {
	config := loadConfigOrExit("config.json")
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	setupGracefulShutdown(cancel)

	rabbitConn := connectRabbitMQOrExit(config)
	defer rabbitConn.Close()

	immudbClient := connectImmudbOrExit(ctx, config)
	defer immudbClient.Disconnect()

	processMessages(ctx, rabbitConn, immudbClient, config.QueueName)
}

func loadConfigOrExit(path string) *Config {
	file, err := os.ReadFile(path)
	if err != nil {
		log.Fatalf("Failed to read config: %v", err)
	}

	var config Config
	if err := json.Unmarshal(file, &config); err != nil {
		log.Fatalf("Failed to parse config: %v", err)
	}

	if config.VirtualHost == "" {
		config.VirtualHost = "/"
	}
	if config.ConnectionTimeout == 0 {
		config.ConnectionTimeout = 10000
	}
	if config.RequestedHeartbeat == 0 {
		config.RequestedHeartbeat = 60
	}

	return &config
}

func setupGracefulShutdown(cancel context.CancelFunc) {
	sigs := make(chan os.Signal, 1)
	signal.Notify(sigs, syscall.SIGINT, syscall.SIGTERM)
	go func() {
		<-sigs
		log.Println("\n🛑 Received shutdown signal")
		cancel()
	}()
}

