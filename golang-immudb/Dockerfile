# syntax=docker/dockerfile:1
FROM golang:1.24.4-alpine as build

WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN go build -o immudb-writer

FROM alpine:3.18
WORKDIR /app
COPY --from=build /app/immudb-writer /app/immudb-writer

# Copy your config.json
COPY config.json .

CMD ["./immudb-writer"]

