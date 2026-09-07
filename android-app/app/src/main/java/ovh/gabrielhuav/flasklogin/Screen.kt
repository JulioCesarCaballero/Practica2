package ovh.gabrielhuav.flasklogin

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Crud : Screen("crud")
}