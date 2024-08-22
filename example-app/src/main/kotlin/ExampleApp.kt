package uk.org.lidalia.example

class ExampleApp(
  private val perRequestAppFactory: PerRequestAppFactory = PerRequestAppFactory(),
) : Handler {

  override operator fun invoke(request: Request): Response {
    val perRequestApp = perRequestAppFactory(request)
    return perRequestApp.invoke(request)
  }
}
