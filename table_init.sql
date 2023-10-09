CREATE TABLE `storage_settings`
(
    `id`         bigint(20) NOT NULL      AUTO_INCREMENT,
    `account_id` varchar(100) DEFAULT NULL,
    `quota_max`  FLOAT DEFAULT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `file_metadata`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `account_id` varchar(100) DEFAULT NULL,
    `file_name` varchar(100) DEFAULT NULL,
    `file_hash` varchar(100) DEFAULT NULL,
    `size` FLOAT DEFAULT NULL,
    PRIMARY KEY (`id`)
);
