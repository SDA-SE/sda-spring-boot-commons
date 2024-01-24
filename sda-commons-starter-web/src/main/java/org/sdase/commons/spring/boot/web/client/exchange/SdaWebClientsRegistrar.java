package org.sdase.commons.spring.boot.web.client.exchange;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;


public class SdaWebClientsRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware,
    EnvironmentAware {

  private ResourceLoader resourceLoader;

  private Environment environment;

  private LinkedHashSet<BeanDefinition> candidateComponents = new LinkedHashSet<>();

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }
  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
      BeanDefinitionRegistry registry) {
    ClassPathScanningCandidateComponentProvider scanner = getScanner();
    scanner.setResourceLoader(this.resourceLoader);
    scanner.addIncludeFilter(new AnnotationTypeFilter(SdaWebClient.class));

    Set<String> basePackages = getBasePackages(importingClassMetadata);
    for (String basePackage : basePackages) {
      candidateComponents.addAll(scanner.findCandidateComponents(basePackage));
    }

    for (BeanDefinition candidateComponent : candidateComponents) {

      try {
        registerBean(candidateComponent, registry);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void registerBean(BeanDefinition candidateComponent, BeanDefinitionRegistry registry)
      throws ClassNotFoundException {

    Class<?> clazz = Class.forName(candidateComponent.getBeanClassName());

    GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
    beanDefinition.setBeanClass(clazz);
    beanDefinition.setFactoryBeanName("sdaHttpServiceProxyFactory");
    beanDefinition.setFactoryMethodName("createProxy");
    beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(clazz);

    registry.registerBeanDefinition(candidateComponent.getBeanClassName(), beanDefinition);
  }

  protected ClassPathScanningCandidateComponentProvider getScanner() {
    return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
      @Override
      protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        boolean isCandidate = false;
        if (beanDefinition.getMetadata().isIndependent()) {
          if (!beanDefinition.getMetadata().isAnnotation()) {
            isCandidate = true;
          }
        }
        return isCandidate;
      }
    };
  }

  protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
    Map<String, Object> attributes = importingClassMetadata
        .getAnnotationAttributes(EnableSdaWebClient.class.getCanonicalName());

    Set<String> basePackages = new HashSet<>();
    for (String pkg : (String[]) attributes.get("basePackages")) {
      if (StringUtils.hasText(pkg)) {
        basePackages.add(pkg);
      }
    }

    if (basePackages.isEmpty()) {
      basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
    }
    return basePackages;
  }

}
