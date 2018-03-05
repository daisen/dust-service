-- dataobj
--
-- create by hst
-- ------------------------------------------------------
-- version 1.0


DROP TABLE IF EXISTS `dataobj`;
CREATE TABLE `dataobj` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `alias` char(50) NOT NULL,
  `app` char(20) NOT NULL,
  `module` char(20) NOT NULL,
  `name` char(50) NOT NULL,
  `table_name` char(50) NOT NULL,
  `conditions` varchar(500) DEFAULT NULL,
  `fix_condition` varchar(100) DEFAULT NULL,
  `orders` varchar(100) DEFAULT NULL,
  `start` int NOT NULL DEFAULT 0,
  `page_size` int NOT NULL DEFAULT 0,
  `fix_where_sql` varchar(500) DEFAULT NULL,
  `order_by_sql` varchar(500) DEFAULT NULL,
  `where_sql` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_alias` (`alias`),
  KEY `idx_table_name` (`table_name`),
  KEY `idx_app` (`app`)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 

DROP TABLE IF EXISTS `dataobj_column`;
CREATE TABLE `dataobj_column` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `master_id` bigint(20) unsigned DEFAULT NULL,
  `name` char(50) NOT NULL,
  `column_name` char(50) NOT NULL,
  `column_label` char(50) NULL,
  `relation_table_name` char(50) NULL,
  `relation_column_name` char(50) NULL,
  `id_column_label` char(50) NULL,
  `data_type` char(20) NOT NULL,
  `mirror_column_label` char(50) DEFAULT NULL,  
  `table_name` char(50) NOT NULL,
  `default_value` char(100) NULL,
  `is_primary_key` tinyint unsigned NOT NULL DEFAULT 0,
  `is_ignore` tinyint unsigned NOT NULL DEFAULT 0,
  `width` int NOT NULL DEFAULT 0,
  `decimal_digits` int NOT NULL DEFAULT 0,
  `is_required` tinyint unsigned NOT NULL DEFAULT 0,
  `is_auto_increment` tinyint unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_column_label` (`column_label`),
  KEY `idx_master_id` (`master_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 

DROP TABLE IF EXISTS `dataobj_table`;
CREATE TABLE `dataobj_table` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `master_id` bigint(20) unsigned NOT NULL,
  `tableName` char(50) NOT NULL,
  `relation_type` char(20) DEFAULT 'LEFT',
  `columnName` char(20) DEFAULT NULL,
  `relationColumn` char(20) DEFAULT NULL,
  `relationWhere` varchar(100) DEFAULT NULL,
  `follow_delete` tinyint unsigned DEFAULT 0,
  `follow_insert` tinyint unsigned DEFAULT 0,
  `follow_update` tinyint unsigned DEFAULT 0,
  PRIMARY KEY (`id`)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 

DROP TABLE IF EXISTS `dataobj_big_text`;
CREATE TABLE `dataobj_big_text` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `code` char(50) NOT NULL,
  `content` text DEFAULT NULL,
  PRIMARY KEY (`id`)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 

DROP TABLE IF EXISTS `dataobj_app`;
CREATE TABLE `dataobj_app` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `app` char(20) NOT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`id`)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 

DROP TABLE IF EXISTS `dataobj_module`;
CREATE TABLE `dataobj_module` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `master_id` bigint(20) unsigned NOT NULL,
  `module` char(20) NOT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_app_module` (`master_id`,`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 