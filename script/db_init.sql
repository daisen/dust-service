DROP TABLE IF EXISTS `dust_db_access`;
CREATE TABLE `dust_db_access` (
  `id` bigint(100) unsigned NOT NULL AUTO_INCREMENT,
  `status` char(100) DEFAULT NULL,
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  `cluster` char(100) NOT NULL,
  `name` char(100) NOT NULL,
  `host` char(100) NOT NULL,
  `access_level` char(100) DEFAULT NULL,
  `permission` char(100) DEFAULT NULL,
  `user` char(100) NOT NULL,
  `password` char(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 

DROP TABLE IF EXISTS `dust_app_config`;
CREATE TABLE `dust_app_config` (
  `id` bigint(100) unsigned NOT NULL AUTO_INCREMENT,
  `status` char(100) DEFAULT NULL,
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL,
  `tenant_id` char(100) NOT NULL,
  `db_access_id` char(100) NOT NULL,
  `app_id` char(100) NOT NULL,
  `app_alias` char(100) NOT NULL,
  `db_name` char(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 