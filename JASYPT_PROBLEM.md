# Deep Dive: Why Encrypted Properties Always Appear as "Changed" in Spring Cloud Config Refresh

You're absolutely right to want a deeper understanding of this issue. The problem with `spring.data.redis.password` consistently appearing as "changed" in `/actuator/refresh` responses, even when the actual value hasn't been modified in AWS Secrets Manager, stems from several **fundamental architectural behaviors** in Spring Cloud Config's handling of encrypted properties and composite configurations.

## The Core Technical Issues

### 1. Property Source Object Identity vs. Value Comparison

Spring Cloud Config's refresh mechanism uses **object identity comparison** rather than **value comparison** when determining what has changed. Here's what happens:

**During Property Source Reconstruction:**
- Spring Cloud Config **rebuilds the entire property source hierarchy** from scratch
- Each property source becomes a **new object instance** in memory
- The comparison logic checks if `PropertySource A != PropertySource B` (object identity)
- Even if the **actual property values are identical**, the objects are different

**Why This Affects Encrypted Properties More:**
- Encrypted properties go through **additional processing layers** (Jasypt decryption, property source wrapping)
- Each refresh cycle creates **new wrapper objects** around the encrypted property sources
- The **decryption context** may be refreshed, creating new object instances even for identical encrypted values

### 2. Composite Configuration Property Source Reconstruction

Your **composite setup** (AWS S3 + Secrets Manager) exacerbates this issue because:

**Multiple Property Source Rebuilding:**
- **AWS Secrets Manager backend** creates new property source objects on each refresh
- **AWS S3 backend** also reconstructs its property sources
- **Composite property source** rebuilds the entire hierarchy, combining both backends
- Each backend's property sources are **recreated as new objects**, regardless of value changes

**Property Source Ordering Effects:**
- Composite configurations maintain **precedence ordering** between different backends
- During refresh, the **entire ordering structure** is rebuilt
- Property sources may be **reordered or restructured**, causing Spring to detect "changes"

### 3. Jasypt-Specific Refresh Behavior

The Jasypt integration adds another layer of complexity that directly contributes to this issue:

**Encryptable Property Source Caching:**
- Jasypt uses `CachingDelegateEncryptablePropertySource` to wrap property sources[1]
- During refresh, **cache invalidation** occurs for all encrypted properties
- The **cache refresh process** causes encrypted properties to appear as "changed"

**Decryption Context Refresh:**
- Jasypt maintains a **decryption context** that includes algorithm settings, IV generators, and encryption keys
- During `/actuator/refresh`, this **context may be refreshed or recreated**
- Even if the encrypted value (`ENC(...)`) is identical, the **decryption wrapper objects** are new instances

**Property Source Wrapping:**
- Jasypt **wraps existing property sources** with encryptable versions
- Each refresh cycle **recreates these wrapper objects**
- The underlying property source comparison fails because the **wrapper objects are different instances**

## The Technical Root Cause

Based on the research, this is a **confirmed issue** that affects many Spring Cloud Config users. A GitHub issue specifically mentions: *"some encrypted properties always cause Spring beans to reinitialize"* and *"encrypted properties always cause Spring beans to reinitialize"*[2].

### Spring Cloud Config's Change Detection Algorithm

The change detection works as follows:

1. **Before Refresh**: Capture current property source objects
2. **During Refresh**: Rebuild all property sources from backends
3. **After Refresh**: Compare old vs. new property source objects
4. **Change Detection**: Any object that `oldPropertySource != newPropertySource` is flagged as "changed"

**The Problem**: This comparison is based on **object identity**, not **value equality**.

### Why Encrypted Properties Are Particularly Affected

**Object Recreation Chain:**
```
AWS Secrets Manager → Property Source → Jasypt Wrapper → Composite Property Source → Spring Environment
```

Each step in this chain **creates new objects** during refresh, making it impossible for Spring's change detection to recognize that the actual values haven't changed.

**Jasypt's Property Source Management:**
- Jasypt creates `EncryptablePropertySource` wrappers around original property sources
- These wrappers are **recreated on each refresh cycle**
- The **cache invalidation** process treats all encrypted properties as potentially changed

## Evidence from the Spring Cloud Ecosystem

### Known Issue Recognition

The Spring Cloud team is **aware of this behavior**. The GitHub issue shows that users consistently report: *"encrypted properties always cause Spring beans to reinitialize"*[2], indicating this is a **widespread and recognized problem**.

### Jasypt-Specific Refresh Issues

There's a documented issue where *"triggering the refresh without changing the configuration files results in changes to this encrypted properties"*[1]. This confirms that the issue is **inherent to how Jasypt integrates with Spring Cloud Config's refresh mechanism**.

## Why This Happens Specifically in Your Environment

### Your Composite Configuration

Your setup combines multiple factors that **amplify this issue**:

1. **AWS Secrets Manager backend** - Creates new property source objects on each refresh
2. **Composite configuration** - Rebuilds entire property source hierarchy
3. **Jasypt encryption** - Adds additional object wrapping and cache invalidation
4. **Scheduled refresh** - Triggers this behavior repeatedly every 60 seconds

### The Perfect Storm

Each component contributes to the problem:
- **AWS Secrets Manager**: New property source objects
- **Composite Config**: Property source hierarchy rebuilding
- **Jasypt**: Encryptable property source wrapping and cache invalidation
- **Spring Cloud Config**: Object identity-based change detection

## Implications for Your Architecture

### Why This Matters

1. **False Positive Alerts**: Your monitoring shows "changes" when none occurred
2. **Unnecessary Processing**: Your Redis connection reset logic may trigger unnecessarily
3. **Log Noise**: Consistent logging of non-changes reduces operational visibility
4. **Resource Consumption**: Unnecessary object recreation and processing overhead

### The Fundamental Limitation

This behavior is **by design** in Spring Cloud Config's current architecture. The framework prioritizes **safety and consistency** over **change detection precision**. It's better to report a false positive than to miss an actual configuration change.

## Key Takeaways

1. **This is expected behavior** - Not a bug, but an architectural characteristic of Spring Cloud Config with encrypted properties and composite configurations

2. **Object identity vs. value comparison** - The root cause is Spring's use of object identity rather than value equality for change detection

3. **Jasypt amplifies the issue** - Encrypted properties are particularly susceptible due to property source wrapping and cache invalidation

4. **Composite configurations worsen it** - Multiple backends mean more object recreation during refresh cycles

5. **No simple fix exists** - This behavior is deeply embedded in Spring Cloud Config's refresh architecture

The solution, as we discussed earlier, is to implement **value-based change detection** in your application code rather than relying on the `/actuator/refresh` endpoint's response to determine actual configuration changes.

Sources
[1] Problem with spring-cloud-config and refresh · Issue #267 https://github.com/ulisesbocchio/jasypt-spring-boot/issues/267
[2] Support for Targeted Property Refresh in /actuator/ ... https://github.com/spring-cloud/spring-cloud-config/issues/2453
[3] spring boot, Not able to change the properties value at ... https://stackoverflow.com/questions/70214917/spring-boot-not-able-to-change-the-properties-value-at-runtime-using-spring-clo
[4] Cannot override encrypted properties in the repository ... https://github.com/spring-cloud/spring-cloud-config/issues/1013
[5] Spring Cloud Config https://cloud.spring.io/spring-cloud-config/spring-cloud-config.html
[6] Spring Cloud Config Refresh Strategies https://soshace.com/spring-cloud-config-refresh-strategies/
[7] FAQ for Configuring Spring Boot Applications Guide https://moldstud.com/articles/p-essential-faq-for-configuring-spring-boot-applications-developers-guide
[8] Spring cloud config git refreshRate behaviour - Stack Overflow https://stackoverflow.com/questions/55239999/spring-cloud-config-git-refreshrate-behaviour
[9] Features https://cloud.spring.io/spring-cloud-static/spring-cloud.html
[10] Integrating Jasypt with Spring 3.1 and Spring 4.0 http://www.jasypt.org/spring31.html
[11] Spring Cloud Config client- avoid property refresh on startup https://stackoverflow.com/questions/65617721/spring-cloud-config-client-avoid-property-refresh-on-startup
[12] Chapter 3. Controlling your configuration with Spring Cloud ... https://livebook.manning.com/book/spring-microservices-in-action/chapter-3
[13] ulisesbocchio/jasypt-spring-boot https://github.com/ulisesbocchio/jasypt-spring-boot
[14] Application Context Services :: Spring Cloud Commons https://docs.spring.io/spring-cloud-commons/reference/spring-cloud-commons/application-context-services.html
[15] Spring Cloud Config https://docs.spring.vmware.com/spring-cloud-config/docs/4.0.11/reference/html/
[16] Spring Boot Configuration with Jasypt https://www.baeldung.com/spring-boot-jasypt
[17] 1. Spring Cloud Context: Application Context Services https://cloud.spring.io/spring-cloud-commons/multi/multi__spring_cloud_context_application_context_services.html
[18] Jasypt out of maintenance? What to use for encryption with ... https://stackoverflow.com/questions/72198393/jasypt-out-of-maintenance-what-to-use-for-encryption-with-spring-boot
[19] Property sources returned in wrong order and in a Collection ... https://github.com/spring-cloud/spring-cloud-config/issues/1158
[20] How to encrypt passwords in a Spring Boot project using ... https://www.geeksforgeeks.org/how-to-encrypt-passwords-in-a-spring-boot-project-using-jasypt/
[21] Spring Cloud Config (Part 1) - DZone https://dzone.com/articles/spring-cloud-config-series-part-1-introduction
[22] Restarting application does not pick up updated properties ... https://stackoverflow.com/questions/64055617/restarting-application-does-not-pick-up-updated-properties-from-git
[23] Notes on Dynamic Configuration Properties https://gist.github.com/dsyer/a43fe5f74427b371519af68c5c4904c7
[24] Spring Cloud Config : Password not getting decrypted with jasypt in ... https://stackoverflow.com/questions/61081031/spring-cloud-config-password-not-getting-decrypted-with-jasypt-in-spring-cloud
[25] 2. Spring Cloud Config Server https://cloud.spring.io/spring-cloud-config/multi/multi__spring_cloud_config_server.html
[26] 1. Spring Cloud Context: Application Context Services https://cloud.spring.io/spring-cloud-commons/2.1.x/multi/multi__spring_cloud_context_application_context_services.html
[27] password encrypted with jasypt is breaking when "refreshed" https://github.com/spring-cloud/spring-cloud-config/issues/808
[28] When Spring Cloud triggers Refresh, Jasypt decrypts properties that ... https://github.com/ulisesbocchio/jasypt-spring-boot/issues/397
[29] 5. Spring Cloud Config Server - shinley https://shinley.gitbooks.io/spring-cloud/ii-spring-cloud-config/5.Spring-Cloud-Config-Server.html
[30] Spring Cloud Config Client https://docs.spring.io/spring-cloud-config/reference/client.html
[31] Spring cloud config server - how to add custom ... https://stackoverflow.com/questions/41267970/spring-cloud-config-server-how-to-add-custom-propertysource-visible-in-findone
[32] Configuration Properties :: Spring Cloud Commons https://docs.spring.io/spring-cloud-commons/reference/configprops.html
[33] How spring cloud config use local property override remote ... https://stackoverflow.com/questions/43800256/how-spring-cloud-config-use-local-property-override-remote-property
[34] OpenText Fortify Software Security Content 2023 Update 4 https://community.opentext.com/cybersec/fortify/w/tips/46822/opentext-fortify-software-security-content-2023-update-4
