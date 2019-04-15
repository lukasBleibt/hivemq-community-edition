/*
 * Copyright 2019 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.extensions.ioc;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.hivemq.bootstrap.ioc.lazysingleton.LazySingleton;
import com.hivemq.bootstrap.ioc.lazysingleton.LazySingletonScope;
import com.hivemq.configuration.info.SystemInformation;
import com.hivemq.configuration.info.SystemInformationImpl;
import com.hivemq.configuration.service.FullConfigurationService;
import com.hivemq.extension.sdk.api.services.auth.SecurityRegistry;
import com.hivemq.extension.sdk.api.services.builder.RetainedPublishBuilder;
import com.hivemq.extension.sdk.api.services.builder.TopicSubscriptionBuilder;
import com.hivemq.extension.sdk.api.services.publish.RetainedMessageStore;
import com.hivemq.extension.sdk.api.services.subscription.SubscriptionStore;
import com.hivemq.extensions.HiveMQPlugins;
import com.hivemq.extensions.PluginBootstrap;
import com.hivemq.extensions.ioc.annotation.PluginStartStop;
import com.hivemq.extensions.loader.*;
import com.hivemq.extensions.services.auth.Authenticators;
import com.hivemq.mqtt.handler.publish.IncomingPublishService;
import com.hivemq.mqtt.services.InternalPublishService;
import com.hivemq.mqtt.services.PublishDistributor;
import com.hivemq.mqtt.topic.tree.LocalTopicTree;
import com.hivemq.mqtt.topic.tree.TopicTreeImpl;
import com.hivemq.persistence.ChannelPersistence;
import com.hivemq.persistence.clientsession.ClientSessionPersistence;
import com.hivemq.persistence.clientsession.ClientSessionSubscriptionPersistence;
import com.hivemq.persistence.ioc.annotation.Persistence;
import com.hivemq.persistence.retained.RetainedMessagePersistence;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import util.TestConfigurationBootstrap;

import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Georg Held
 */
@SuppressWarnings("NullabilityAnnotations")
public class PluginModuleTest {

    private Injector injector;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {

                install(new PluginModule());
                bind(SystemInformation.class).toInstance(new SystemInformationImpl());
                bind(ChannelPersistence.class).toInstance(mock(ChannelPersistence.class));
                bind(FullConfigurationService.class).toInstance(new TestConfigurationBootstrap().getFullConfigurationService());
                bind(ClientSessionPersistence.class).toInstance(mock(ClientSessionPersistence.class));
                bind(PublishDistributor.class).toInstance(mock(PublishDistributor.class));
                bind(LocalTopicTree.class).toInstance(mock(TopicTreeImpl.class));
                bind(IncomingPublishService.class).toInstance(mock(IncomingPublishService.class));
                bind(RetainedMessagePersistence.class).toInstance(mock(RetainedMessagePersistence.class));
                bind(ListeningExecutorService.class).annotatedWith(Persistence.class).toInstance(mock(ListeningExecutorService.class));
                bind(InternalPublishService.class).toInstance(mock(InternalPublishService.class));
                bind(ClientSessionSubscriptionPersistence.class).toInstance(mock(ClientSessionSubscriptionPersistence.class));
                bindScope(LazySingleton.class, LazySingletonScope.get());
            }
        });
    }

    @Test(timeout = 5000)
    public void test_hivemqplugins_is_singleton() {
        final HiveMQPlugins instance1 = injector.getInstance(HiveMQPlugins.class);
        final HiveMQPlugins instance2 = injector.getInstance(HiveMQPlugins.class);

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test(timeout = 5000)
    public void test_bootstrap_is_singleton() {
        final PluginBootstrap instance1 = injector.getInstance(PluginBootstrap.class);
        final PluginBootstrap instance2 = injector.getInstance(PluginBootstrap.class);

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test(timeout = 5000)
    public void test_initializer_is_singleton() {
        final PluginStaticInitializer instance1 = injector.getInstance(PluginStaticInitializer.class);
        final PluginStaticInitializer instance2 = injector.getInstance(PluginStaticInitializer.class);

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test(timeout = 5000)
    public void test_plugin_factory_is_singleton() {
        final HiveMQPluginFactory instance1 = injector.getInstance(HiveMQPluginFactory.class);
        final HiveMQPluginFactory instance2 = injector.getInstance(HiveMQPluginFactory.class);

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test(timeout = 5000)
    public void test_loader_is_singleton() {
        final PluginLoader instance1 = injector.getInstance(PluginLoader.class);
        final PluginLoader instance2 = injector.getInstance(PluginLoader.class);

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test(timeout = 5000)
    public void test_service_dependencies_is_singleton() {
        final PluginServicesDependencies instance1 = injector.getInstance(PluginServicesDependencies.class);
        final PluginServicesDependencies instance2 = injector.getInstance(PluginServicesDependencies.class);

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test(timeout = 5000)
    public void test_builder_dependencies_is_singleton() {
        final PluginBuilderDependencies instance1 = injector.getInstance(PluginBuilderDependencies.class);
        final PluginBuilderDependencies instance2 = injector.getInstance(PluginBuilderDependencies.class);

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test(timeout = 5000)
    public void test_retained_publish_builder_is_not_singleton() {
        final RetainedPublishBuilder instance1 = injector.getInstance(RetainedPublishBuilder.class);
        final RetainedPublishBuilder instance2 = injector.getInstance(RetainedPublishBuilder.class);

        assertNotNull(instance1);
        assertNotSame(instance1, instance2);
    }

    @Test(timeout = 5000)
    public void test_retained_message_store_is_singleton() {
        final RetainedMessageStore instance1 = injector.getInstance(RetainedMessageStore.class);
        final RetainedMessageStore instance2 = injector.getInstance(RetainedMessageStore.class);

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test(timeout = 5000)
    public void test_topic_subscription_builder_is_not_singleton() {
        final TopicSubscriptionBuilder instance1 = injector.getInstance(TopicSubscriptionBuilder.class);
        final TopicSubscriptionBuilder instance2 = injector.getInstance(TopicSubscriptionBuilder.class);

        assertNotNull(instance1);
        assertNotSame(instance1, instance2);
    }

    @Test(timeout = 5000)
    public void test_subscription_store_is_singleton() {
        final SubscriptionStore instance1 = injector.getInstance(SubscriptionStore.class);
        final SubscriptionStore instance2 = injector.getInstance(SubscriptionStore.class);

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test(timeout = 5000)
    public void test_lifecycle_handler_is_singleton() {
        final PluginLifecycleHandler instance1 = injector.getInstance(PluginLifecycleHandler.class);
        final PluginLifecycleHandler instance2 = injector.getInstance(PluginLifecycleHandler.class);

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test(timeout = 5000)
    public void test_authenticators_is_singleton() {
        final Authenticators instance1 = injector.getInstance(Authenticators.class);
        final Authenticators instance2 = injector.getInstance(Authenticators.class);

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test(timeout = 5000)
    public void test_security_registry_is_singleton() {
        final SecurityRegistry instance1 = injector.getInstance(SecurityRegistry.class);
        final SecurityRegistry instance2 = injector.getInstance(SecurityRegistry.class);

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test(timeout = 5000)
    public void test_annotated_plugin_start_stop_executor_service_is_singleton() {
        final ExecutorService instance1 = injector.getInstance(Key.get(ExecutorService.class, PluginStartStop.class));
        final ExecutorService instance2 = injector.getInstance(Key.get(ExecutorService.class, PluginStartStop.class));

        assertSame(instance1, instance2);
    }
}