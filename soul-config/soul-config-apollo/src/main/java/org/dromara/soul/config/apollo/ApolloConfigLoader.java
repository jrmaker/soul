/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * Contributor license agreements.See the NOTICE file distributed with
 * This work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * he License.You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dromara.soul.config.apollo;

import org.dromara.soul.common.extension.Join;
import org.dromara.soul.common.utils.StringUtils;
import org.dromara.soul.config.api.ConfigEnv;
import org.dromara.soul.config.api.ConfigException;
import org.dromara.soul.config.api.ConfigLoader;
import org.dromara.soul.config.api.ConfigParent;
import org.dromara.soul.config.api.property.MapPropertyKeySource;
import org.dromara.soul.config.api.property.PropertyKeySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The type Apollo config loader.
 *
 * @author xiaoyu
 */
@Join
public class ApolloConfigLoader implements ConfigLoader<ApolloConfig> {

    private static final Logger logger = LoggerFactory.getLogger(ApolloConfigLoader.class);

    public ApolloConfigLoader() {
        ConfigEnv.getInstance().putBean(new ApolloConfig());
    }

    @Override
    public void load(Supplier<Context> context, LoaderHandler<ApolloConfig> handler) {
        LoaderHandler<ApolloConfig> apolloHandler = (c, config) -> apolloLoad(c, handler, config);
        againLoad(context, apolloHandler, ApolloConfig.class);
    }

    private void apolloLoad(Supplier<Context> context, LoaderHandler<ApolloConfig> handler, ApolloConfig apolloConfig) {
        if (apolloConfig != null) {
            check(apolloConfig);
            logger.info("loader apollo config: {}", apolloConfig);
            Map<String, Object> resultMap = ApolloClient.pull(apolloConfig);
            if (resultMap.size() > 0) {
                List<PropertyKeySource<?>> propertySources = new ArrayList<>();
                propertySources.add(new MapPropertyKeySource("apollo", resultMap));
                context.get().getOriginal().load(() -> context.get().withSources(propertySources), this::apolloFinish);
            }
            handler.finish(context, apolloConfig);
        } else {
            throw new ConfigException("apollo config is null");
        }
    }

    private void apolloFinish(Supplier<Context> context, ConfigParent config) {
        logger.info("apollo loader config {}:{}", config != null ? config.prefix() : "", config);
    }

    private void check(ApolloConfig config) {
        if (StringUtils.isBlank(config.getAppId())) {
            throw new ConfigException("apollo.appId is null");
        }
        if (StringUtils.isBlank(config.getMetaServer())) {
            throw new ConfigException("apollo.metaServer is null");
        }
    }
}
