# kafkaDemo
1. 搭建zookeeper集群
	1.1 配置~/kafka/zookeeper-3.4.8/conf/zoo.cfg
		# zookeeper数据存储
		dataDir=/home/zf/kafka/data_zk
		# zookeeper日志存储
		dataLogDir=/home/zf/kafka/log_zk
		# zookeeper端口
		clientPort=2186
		# zookeeper集群nodes
		server.0=172.30.12.22:4001:4002
		server.1=172.30.12.33:4001:4002
		server.2=172.30.12.48:4001:4002
	1.2 指定zookeeper集群node
		在~/kafka/data_zk目录下新建"myid"文件，文件内容为zookeeper集群nodes中匹配的0、1、2。
		每个服务器上的zookeeper都需要指定自己的myid。
	1.3 启动zookeeper
		通过命令 ~/kafka/zookeeper-3.4.8/bin/zkServer.sh start 启动每个服务器上zookeeper。
		通过命令 ~/kafka/zookeeper-3.4.8/bin/zkServer.sh status 查看每个zookeeper服务是leader还是flower
2. 启动kafka server
	2.1 配置 ~/kafka/kafka_2.11-0.10.0.0/config/server0.properties (通过server后面的数字区分该server的broker.id)
		# 该server 的 broker.id
		broker.id=0
		# 监控端口，默认为本机地址
		listeners=PLAINTEXT://:9092
		# 向Producer和Consumer建议连接的Hostname和port，最好填写本机IP
		advertised.host.name=172.30.12.22
		advertised.port=9092
		advertised.listeners=PLAINTEXT://172.30.12.22:9092
		# 消息保留的时间，单位为小时
		log.retention.hours=168
		# zookeeper集群地址
		zookeeper.connect=172.30.12.22:2186,172,30,12,33:2186,172.30.12.48:2186
	2.2 启动Kafka server
		通过命令启动kafka serverm，其中不同broker使用不同server.properties。
		由于使用 & 后台运行时有一定几率断开shell后进程中断，故使用 -daemon标记后台运行。
		启动命令如下：
		~/kafka/kafka_2.11-0.10.0.0/bin/kafka-server-start.sh -daemon ~/kafka/kafka_2.11-0.10.0.0/config/server0.properties 
3. 创建topic
	3.1 创建topic：test 一个分区 --partitions 1 一个副本--replication-factor 1
		~/kafka/kafka_2.11-0.10.0.0/bin/kafka-topics.sh --zookeeper 172.30.12.22:2186,172.30.12.33:2186,172.30.12.48:2186 --create --topic test --partitions 1 --replication-factor 1
	3.2 查看所有topic --list
		~/kafka/kafka_2.11-0.10.0.0/bin/kafka-topics.sh --zookeeper 172.30.12.22:2186,172.30.12.33:2186,172.30.12.48:2186 --list

	3.3 查看topic详情 --describe --topic test
		~/kafka/kafka_2.11-0.10.0.0/bin/kafka-topics.sh --zookeeper 172.30.12.22:2186,172.30.12.33:2186,172.30.12.48:2186 --describe --topic test

	3.4 增加topic 分区
		~/kafka/kafka_2.11-0.10.0.0/bin/kafka-topics.sh --zookeeper 172.30.12.22:2186,172.30.12.33:2186,172.30.12.48:2186 --alter --topic test --partitions 3
	3.5 重新分配topic的partition 和 replication-factor
		执行下面命令后，会显示重新分配的json，将该json覆盖到文件~/kafka/partitions-reassign.json中
		~/kafka/kafka_2.11-0.10.0.0/bin/kafka-reassign-partitions.sh --zookeeper 172.30.12.22:2186,172.30.12.33:2186,172.30.12.48:2186 --reassignment-json-file ~/kafka/partitions-reassign.json -topics-to-move-json-file ~/kafka/topic-to-move.json --broker-list 0,1,2 --generate
		执行下面命令生效
		~/kafka/kafka_2.11-0.10.0.0/bin/kafka-reassign-partitions.sh --zookeeper 172.30.12.22:2186,172.30.12.33:2186,172.30.12.48:2186 --reassignment-json-file ~/kafka/partitions-reassign.json -topics-to-move-json-file ~/kafka/topic-to-move.json --broker-list 0,1,2 --execute