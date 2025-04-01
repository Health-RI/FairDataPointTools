# Fair Data Point Tools

## prerequisites

- java 21
- maven 3

```sh
cd FairDataPointTools
mvn install

java -jar target\FairDataPointTools-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Program options:

help

```
help show info on the parameters.

```

display structure

```
-u<URL to FDP>

instead -u you can use --fdp and select form hardcoded list of known FDP servers. 

```

saving fdp to disk:

```
-u=<URL to FDP> save -f=<folder>

instead -u you can use --fdp and select form hardcoded list of known FDP servers. 
```

validating fdp to profile:

```
-u=<URL to FDP> validate -p=<DCAP_AP_2|HEALTH_RI|DCAT_AP_3_BASE|DCAT_AP_3_FULL>
```

Profiles:
DCAT_AP_2= DCAP_AP v2.0
DCAP_AP_3_BASE = DCAT_AP v3.0 only basic validation
DCAT_AP_3_FULL = DCAR_AP v3.0 full validation.
HEALTH_RI = HEALTH_RI v1.0.0
HEALTH_RI_V2 = HEALTH_RI v2.0.0

search FDP using graph embedding.

Experimental search function using a Universal Sentence Encoder. Search description on graph embeddig.
this is experimental feature and needs some still undocumented setup. 