plugins {
    application
}

sourceSets {
    main {
        java {
            srcDirs("src")
        }
    }

    test {
        java {
            srcDirs("test")
        }
    }
}

application {
    mainClass = "com.example.train.DotConvertorApplication"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
}
