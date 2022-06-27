# ACS-Exporter

RHACS exporter is a tool that extracts the network dependency graph in CSV or Excel format for each Openshift cluster defined in a JSON configuration file.

## Build
```
mvn clean install
```

## Run
```
java -jar target/acs-exporter.jar \ 
    -c <config file> \
    -o <output dir> \
    -u <user> \
    -p <pwd>
```

## Debug
```
java -jar target/acs-exporter.jar \ 
    -c <config file> \
    -o <output dir> \
    -u <user> \
    -p <pwd> \
    -v
```

## Config template
```
{
    "acsUrl": "<url>",
    "acsCredentials": "<credentials>",
    "clusters": [
        {
            "id": "<cluster_id>",
            "name": "<name>",
            "env": "<environment>",
            "url": "<url>"
        }
    ]
}
```

##  Usage
```
usage: Usage:
 -c,--config <config>       Set exporter config file
 -o,--output <output>       Set exporter output dir
 -p,--passwd <passwd>       Set OCP password
 -u,--user <user>           Set OCP username
 -n,--namespace <namespace> Set OCP namespace (optional)
 -e,--env <environment>     Set OCP environment (optional)
 -v,--verbose               Set verbose mode (optional)
```

## Sample csv output 
```csv
Namespace, Kind, Name, Flow Direction, Namespace, Name, Port
<source namespace>, <kind>, <pod name>, <flow direction>, <target namespace>, <name>, <port/proto>
```
