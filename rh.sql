create table users
(
    uuid       uuid                                   not null
        constraint users_pk
            primary key,
    email      varchar                                not null
        constraint users_pk_email
            unique,
    user_name  varchar                                not null
        constraint users_pk_name
            unique,
    password   varchar                                not null,
    create_at  timestamp with time zone default now() not null,
    update_at  integer,
    delete_at  timestamp with time zone,
    version    integer                                not null,
    avatar     varchar,
    last_login timestamp with time zone,
    last_off   timestamp with time zone
);

comment on table users is '用户表';

comment on column users.uuid is 'UUID';

comment on column users.email is '邮箱';

comment on column users.user_name is '用户名';

comment on column users.password is '密码';

comment on column users.create_at is '创建时间';

comment on column users.update_at is '更新时间';

comment on column users.delete_at is '软删除时间';

comment on column users.version is '版本号';

comment on column users.avatar is '头像';

comment on column users.last_login is '最后登录时间';

comment on column users.last_off is '最后离线时间';

alter table users
    owner to postgres;



create table role_permission
(
    uuid      uuid                                                         not null
        constraint role_permission_uuid
            primary key,
    role      varchar                  default 'NORMAL'::character varying not null,
    version   integer                  default 0                           not null,
    create_at timestamp with time zone default now()                       not null,
    update_at timestamp with time zone
);

comment on table role_permission is '用户权限表';

comment on column role_permission.uuid is '用户UUID';

comment on column role_permission.role is '角色';

comment on column role_permission.version is '版本号';

comment on column role_permission.create_at is '创建时间';

comment on column role_permission.update_at is '更新时间';

alter table role_permission
    owner to postgres;

create table friend
(
    uuid      uuid                                   not null
        constraint friend_pk_uuid
            primary key,
    chat_id   varchar                                not null,
    user_id   uuid                                   not null,
    friend_id uuid                                   not null,
    status    boolean                  default false not null,
    create_at timestamp with time zone default now() not null,
    update_at timestamp with time zone,
    delete_at timestamp with time zone,
    version   integer                  default 0     not null,
    "group"   boolean                  default false not null
);

comment on table friend is '好友表';

comment on column friend.uuid is 'UUID';

comment on column friend.chat_id is '会话ID';

comment on column friend.user_id is '发起人ID';

comment on column friend.friend_id is '好友ID';

comment on column friend.status is '状态';

comment on column friend.create_at is '创建时间';

comment on column friend.update_at is '更新时间';

comment on column friend.delete_at is '拉黑时间';

comment on column friend.version is '版本号';

comment on column friend."group" is '是否是群组';

alter table friend
    owner to postgres;

create unique index friend_index_ufid
    on friend (user_id, friend_id);

create table message
(
    id             bigserial
        constraint message_pk_id
            primary key,
    sender_uuid    uuid                                   not null,
    recipient_uuid uuid                                   not null,
    content        varchar,
    file           varchar,
    create_at      timestamp with time zone default now() not null,
    delete_at      timestamp with time zone,
    version        integer                  default 0     not null
);

comment on table message is '消息表';

comment on column message.id is '自增ID';

comment on column message.sender_uuid is '发送者';

comment on column message.recipient_uuid is '接受者UUID';

comment on column message.content is '消息内容';

comment on column message.file is '文件链接';

comment on column message.create_at is '创建时间';

comment on column message.delete_at is '删除时间';

comment on column message.version is '版本号';

alter table message
    owner to postgres;

create table "group"
(
    uuid       uuid                                   not null
        constraint group_pk
            primary key,
    group_name varchar                                not null
        constraint group_pk_name
            unique,
    owner_id   uuid                                   not null,
    notice     varchar,
    version    integer                  default 0     not null,
    create_at  timestamp with time zone default now() not null,
    avatar     varchar
);

comment on table "group" is '群组表';

comment on column "group".uuid is 'UUID';

comment on column "group".group_name is '群组名称';

comment on column "group".owner_id is '群主UUID';

comment on column "group".notice is '群公告';

comment on column "group".version is '版本号';

comment on column "group".create_at is '创建时间';

comment on column "group".avatar is '群头像';

alter table "group"
    owner to postgres;