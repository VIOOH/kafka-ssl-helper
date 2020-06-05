/*
 *
 * Copyright 2019-2020 VIOOH Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package viooh.kafka_ssl_helper;


import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.Keyword;


/**
 * SSL utility class for kafka consumers/producers.
 * Allow you to use the regular kafka clients with SSL by supplying in memory certificates.
 *
 */
public class KafkaSSLHelper {

    static {
        Clojure.var("clojure.core", "require")
            .invoke( Clojure.read("kafka-ssl-helper.core"));
        Clojure.var("clojure.core", "require")
            .invoke( Clojure.read("clojure.walk"));
    }
    private static final IFn keywordizeFn = Clojure.var("clojure.walk", "keywordize-keys");
    private static final IFn stringifyFn = Clojure.var("clojure.walk", "stringify-keys");

    private static final IFn intoFn = Clojure.var("clojure.core", "into");

    private static final Object EMPTY_MAP = Clojure.var("clojure.core", "hash-map").invoke();

    private static final IFn sslOptsFn = Clojure.var("kafka-ssl-helper.core",
                                                     "ssl-opts");



    @SuppressWarnings("unchecked")
    /**
     * <p>Return valid configuration options for a kafka consumer/producer using ssl auth.
     * </p>
     * @param config the map of parameters for the consumer/producer. Should contain a "private-key", "ca-cert" and an optional "ca-cert-pem" entry (only the keystore is generated if the `ca-cert-pem` is not supplied).
     * @return a config map with pointers to temporary keystores and passwords
     */
    public static Map<String,? extends Object> sslOpts(Map<String,? extends Object> config){
        if( config == null ){
            return null;
        }

        final Map<? extends Object, ? extends Object> cljMap = (Map<String, ? extends Object>) intoFn.invoke(EMPTY_MAP, config);

        final Map<String, ? extends Object> configWithSSLOpts = (Map<String, ? extends Object>) stringifyFn.invoke(sslOptsFn.invoke(keywordizeFn.invoke(cljMap)));

        return configWithSSLOpts;
    }
}
