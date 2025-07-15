CREATE TABLE users
(
    id         uuid                     DEFAULT gen_random_uuid() PRIMARY KEY,
    username   varchar(50)                            NOT NULL UNIQUE,
    password   varchar(255)                           NOT NULL,
    email      varchar(100) UNIQUE,
    role       varchar(20)                            NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);

CREATE TABLE chats
(
    id         uuid                     DEFAULT gen_random_uuid() PRIMARY KEY,
    name       varchar(100)                           NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);

CREATE TABLE chat_users
(
    chat_id   uuid                                   NOT NULL,
    user_id   uuid                                   NOT NULL,
    joined_at timestamp with time zone DEFAULT now() NOT NULL,
    PRIMARY KEY (chat_id, user_id),
    CONSTRAINT fk_chat FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE chat_messages
(
    id        uuid                     DEFAULT gen_random_uuid() PRIMARY KEY,
    chat_id   uuid                                   NOT NULL,
    sender_id uuid                                   NOT NULL,
    text      text                                   NOT NULL,
    sent_at   timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT fk_message_chat FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE,
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE SET NULL
);