# Kotlin Shell Extension
[ ![Download](https://api.bintray.com/packages/jakubriegel/KotlinShell/kotlin-shellextension/images/download.svg?version=0.2) ](https://bintray.com/jakubriegel/KotlinShell/kotlin-shellextension/0.2/link)

## get it
```kotlin
repositories {
    maven("https://dl.bintray.com/jakubriegel/KotlinShell")
}

dependencies {
    implementation("eu.jrie.jetbrains:kotlin-shellextension:0.1")
}
```

Kotlin Shell Extension features slf4j logging. To use it add logging implementation or NOP logger to turn it off: 
```kotlin
implementation("org.slf4j:slf4j-nop:1.7.26")
```

## usage
### writing scripts
with new coroutine scope:
```kotlin
shell {
    "echo hello world!"()
}
```

with given scope:
```kotlin
shell (
    scope = myScope
) {
    "echo hello world!"()
}
```

### processes
starting system process with dsl:
```kotlin
val echo = systemProcess {
    cmd { "echo" withArg "hello" }
}
echo()
```

starting system with extensions:
```kotlin
val echo = "echo hello".process() 
echo()
```
or simply: 
```kotlin
"echo hello"() 
```

### pipelines
```kotlin
process1 pipe process2 pipe process3
```

 