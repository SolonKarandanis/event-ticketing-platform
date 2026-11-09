package com.etp.ticketservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

class RepositoryConventionsTest {

    @Test
    void everyRepositoryMethodDeclaresAnExplicitQuery() throws Exception {
        // The default scanner only accepts concrete classes, so it would silently find
        // nothing here and this test would always pass.
        var scanner =
                new ClassPathScanningCandidateComponentProvider(false) {
                    @Override
                    protected boolean isCandidateComponent(AnnotatedBeanDefinition definition) {
                        return definition.getMetadata().isInterface();
                    }
                };
        scanner.addIncludeFilter(new AssignableTypeFilter(Repository.class));

        var repositories = scanner.findCandidateComponents("com.etp.ticketservice");
        assertThat(repositories).as("repositories found by the scan").isNotEmpty();

        List<String> missing = new ArrayList<>();
        for (BeanDefinition definition : repositories) {
            for (Method method :
                    Class.forName(definition.getBeanClassName()).getDeclaredMethods()) {
                if (!method.isSynthetic()
                        && !method.isDefault()
                        && !Modifier.isStatic(method.getModifiers())
                        && !method.isAnnotationPresent(Query.class)) {
                    missing.add(
                            method.getDeclaringClass().getSimpleName() + "#" + method.getName());
                }
            }
        }

        assertThat(missing).as("repository methods without @Query").isEmpty();
    }
}
