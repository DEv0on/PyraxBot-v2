dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive:${properties["springboot.version"]}")

    implementation("dev.arbjerg:lavalink-client:${properties["lavalink.version"]}")
}