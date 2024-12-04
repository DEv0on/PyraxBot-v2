dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive:${properties["springboot.version"]}")

    implementation("dev.arbjerg:lavalink-client:${properties["lavalink.version"]}")

    implementation("com.squareup.retrofit2:retrofit:${properties["retrofit.version"]}")
    implementation("com.squareup.retrofit2:converter-gson:${properties["retrofit.version"]}")
    implementation("com.jakewharton.retrofit:retrofit2-reactor-adapter:2.1.0")
}