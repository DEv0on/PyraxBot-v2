FROM rabbitmq:4.0-management-alpine

RUN apk --no-cache add curl

RUN curl -L https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v4.0.2/rabbitmq_delayed_message_exchange-4.0.2.ez > $RABBITMQ_HOME/plugins/rabbitmq_delayed_message_exchange-4.0.2.ez

RUN chown rabbitmq:rabbitmq $RABBITMQ_HOME/plugins/rabbitmq_delayed_message_exchange-4.0.2.ez

RUN rabbitmq-plugins enable --offline rabbitmq_delayed_message_exchange