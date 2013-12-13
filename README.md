# Scalding Example

This example goes beyond the simple word count program and introduces the following concepts:

1. Mapping data fields to case classes
2. Incorporating JSON based configuration
3. Using the 'RichDate' library to perform range scans in HDFS dated directories and to perform common date related tasks

#### Scala Version:

This must be run against:  
<b>Scala version 2.9.2</b> (extra configuration is required for newer versions)

#### Running the example:

##### 1. Setup
		
		sbt/sbt eclipse
		sbt/sbt assembly

##### 2. Running in Local Mode

		sbt/sbt \
		"run example.analytics.Main \
		--impressions data/input/impression.log \
		--clicks data/input/click.log \
		--output data/output \
		--date 2012-10-01 2012-10-02 \
		--local"