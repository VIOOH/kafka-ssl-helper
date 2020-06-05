# kafka-ssl-helper

Remove the hassle of handling certificates & keystores as files.

Temporary secured keystores are generated on the fly when you need
them for your kafka consumers/producers.

The keystore/truststore have passwords generated randomly at runtime
using `java.security.SecureRandom`.

## Getting the latest release
If you don't have a gitub access token, Then create a token under your
Github `Settings/Developer settings/Personal access tokens`
(https://github.com/settings/tokens), whilst making sure that the
token has `read:packages, write:packages and delete:packages`
permissions

Add to your .zshrc/.bashrc the following
``` bash
export GH_PACKAGES_USR=YOUR_GITHUB_USER
export GH_PACKAGES_PSW=SECRET_TOKEN
```

### Using Leiningen
Add the following repository to your `project.clj`:

``` clojure
["github" {:url "https://maven.pkg.github.com/VIOOH/kafka-ssl-helper"
           :username :env/GH_PACKAGES_USR
           :password :env/GH_PACKAGES_PSW}]

```

And add the kafka-ssl-helper dependency: `[com.viooh/kafka-ssl-helper "0.5.0"]`.

### Using Gradle

``` gradle
repositories {
    mavenCentral()
    maven {
        url "https://repo.clojars.org"
    }
    maven {
        url "https://maven.pkg.github.com/VIOOH/kafka-ssl-helper"
        credentials {
            username = System.getenv('GH_PACKAGES_USR')
            password = System.getenv('GH_PACKAGES_PSW')
        }
    }
}
```

## Usage

`ssl-opts` takes the private key and certificate of your consumer/producer which should have been received securely and returns the needed kafka consumer/producer configs (truststore & keystore).

### From Clojure
``` clojure
;; import ssl-opts
(require '[kafka-ssl-helper.core :as ssl-helper])

;; cert-pem, private-key and ca-cert are multi line strings
(ssl-opts {:cert-pem    cert-pem
           :private-key private-key
           :ca-cert-pem ca-cert})
;; resulting map
{:ssl.keystore.location   "ks.jks"
 :ssl.keystore.password   "GeneratedPassword"
 :ssl.truststore.location "ts.jks"
 :ssl.truststore.password "GeneratedPassword"
 :security.protocol       "SSL"}

;; usage with a producer
(let [producer (KafkaProducer.
                ^Map (merge kafka-config
                            (ssl-helper/ssl-opts
                             {:cert-pem    cert-pem
                              :private-key private-key
                              :ca-cert-pem ca-cert})))]
  ...
  )
```

## Authors and contributors

Authors and contributors _(in alphabetic order)_

  - Olivier Bohrer ([@obohrer](https://github.com/obohrer))


## License

Copyright © 2019-2020 VIOOH Ltd

Distributed under the Apache License v 2.0 (http://www.apache.org/licenses/LICENSE-2.0)
