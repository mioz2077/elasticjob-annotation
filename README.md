## elastic-job 注解实现

### 使用方式:java
**[注]：** 需在 *src/main/resources* 目录创建 **job.properties** 文件。
内容如下：
```xml
#值为项目中作业类所在的包名
context.scan.base-package = com.ane56.job
```
在maven的pom.xml 文件中指定mainClass
内容如下：
```xml
	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-jar-plugin</artifactId>
				<version>2.4</version>
				<configuration>
					<classesDirectory>target/classes/</classesDirectory>
					<archive>
						<manifest>
							<addClasspath>true</addClasspath>
							<classpathPrefix>lib/</classpathPrefix>
							<mainClass>com.mioz.elasticjob.annotation.Main</mainClass>
							<useUniqueVersions>false</useUniqueVersions>
						</manifest>
					</archive>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-dependency-plugin</artifactId>
				<executions>
					<execution>
						<id>copy-dependencies</id>
						<phase>package</phase>
						<goals>
							<goal>copy-dependencies</goal>
						</goals>
						<configuration>
							<type>jar</type>
							<includeTypes>jar</includeTypes>
							<useUniqueVersions>false</useUniqueVersions>
							<outputDirectory>${project.build.directory}/lib</outputDirectory>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
```

### 使用方式:Spring
**[注]：** 需在 *src/main/resources* 目录创建 **job.properties** 文件。
```xml
#值为项目中作业类所在的包名
context.scan.base-package = com.ane56.job
```
在spring配置xml中添加bean配置，内容如下：
```xml
	<beans id="ejMainClass" class="com.mioz.elasticjob.annotation.MainClass" />
```