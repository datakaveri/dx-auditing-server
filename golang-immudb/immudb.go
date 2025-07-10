package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"strings"

	"github.com/codenotary/immudb/pkg/api/schema"
	"github.com/codenotary/immudb/pkg/client"
)

func connectImmudbOrExit(ctx context.Context, config *Config) client.ImmuClient {
	opts := client.DefaultOptions().
		WithAddress(config.ImmuDatabaseIP).
		WithPort(config.ImmuDatabasePort)

	immuClient, err := client.NewImmuClient(opts)
	if err != nil {
		log.Fatalf("Failed to create immudb client: %v", err)
	}

	_, err = immuClient.Login(ctx,
		[]byte(config.ImmuDatabaseUserName),
		[]byte(config.ImmuDatabasePassword),
	)
	if err != nil {
		log.Fatalf("Failed to login to immudb: %v", err)
	}
	log.Printf("🚀 Now using immudb database: %s", config.ImmuDatabaseName)

	_, err = immuClient.UseDatabase(ctx, &schema.Database{
		DatabaseName: config.ImmuDatabaseName,
	})
	if err != nil {
		log.Fatalf("Failed to use database: %v", err)
	}

	log.Println("✅ Connected to immudb")
	return immuClient
}

func insertActivityLog(ctx context.Context, immudbClient client.ImmuClient, activity ActivityLog) error {
	key := []byte("activity:" + activity.ID)
	value, err := json.Marshal(activity)
	if err != nil {
		return fmt.Errorf("marshal failed: %w", err)
	}

	_, err = immudbClient.Get(ctx, key)
	if err == nil {
		return fmt.Errorf("duplicate")
	} else if strings.Contains(err.Error(), "key not found") {
		// ✅ Safe to insert
	} else {
		log.Printf("⚠️ immudb Get failed: %v", err)
		return fmt.Errorf("immudb get failed: %w", err)
	}

	log.Printf("📌 Final Insert Query - Key: %s, Value: %s", key, string(value))
	_, err = immudbClient.VerifiedSet(ctx, key, value)
	if err != nil {
		log.Printf("⚠️ immudb VerifiedSet failed: %v", err)
		return fmt.Errorf("immudb insert failed: %w", err)
	}

	log.Printf("✅ Successfully stored new activity %s", activity.ID)
	return nil
}

