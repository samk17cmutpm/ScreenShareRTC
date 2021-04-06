package fr.pchab.androidrtc.baseimport android.annotation.SuppressLintimport android.content.Contextimport android.content.Intentimport android.content.pm.PackageManagerimport android.net.Uriimport android.os.Buildimport android.provider.Settingsimport android.text.TextUtilsimport android.widget.Toastimport androidx.appcompat.app.AppCompatActivityimport androidx.appcompat.widget.Toolbarimport androidx.core.app.ActivityCompatimport androidx.core.content.ContextCompatimport androidx.fragment.app.Fragmentimport io.github.inflationx.viewpump.ViewPumpContextWrapper@SuppressLint("Registered")open class BaseActivity : AppCompatActivity() {    companion object {        const val PERMISSIONS_REQUEST_CODE = 6969    }    override fun attachBaseContext(newBase: Context) {        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))    }    fun showFragment(        containerId: Int,        fragment: Fragment?    ) {        fragment?.let {            val ft = supportFragmentManager.beginTransaction()            ft.replace(containerId, it)            ft.commitAllowingStateLoss()        }    }    fun initToolBar(toolbar: Toolbar, title: String? = null, icon: Int? = null) {        setSupportActionBar(toolbar)        toolbar.setContentInsetsAbsolute(0, 0)        if (icon != null) {            supportActionBar?.setDisplayHomeAsUpEnabled(true)            toolbar.setNavigationIcon(icon)        }        if (!TextUtils.isEmpty(title)) {            supportActionBar?.title = title            supportActionBar?.setDisplayShowTitleEnabled(true)        } else {            supportActionBar?.setDisplayShowTitleEnabled(false)        }    }    fun initToolBar(toolbar: Toolbar, icon: Int) {        setSupportActionBar(toolbar)        toolbar.setContentInsetsAbsolute(0, 0)        if (icon != 0) {            supportActionBar?.setDisplayHomeAsUpEnabled(true)            toolbar.setNavigationIcon(icon)        }        supportActionBar?.setDisplayShowTitleEnabled(false)    }    /**     * isSpecificPermissionsGranted     */    fun isSpecificPermissionsGranted(permissions: Array<String>): Boolean {        val size = permissions.size        for (index in 0 until size) {            val result = ContextCompat.checkSelfPermission(this, permissions[index])            if (result != PackageManager.PERMISSION_GRANTED)                return false        }        return true    }    /**     * requestSpecificPermissions     */    fun requestSpecificPermissions(permissions: Array<String>, message: Int) {        val size = permissions.size        var isShouldShowRequestPermissionRationale = false        for (index in 0 until size) {            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[index])) {                isShouldShowRequestPermissionRationale = true                break            }        }        if (isShouldShowRequestPermissionRationale) {            Toast.makeText(this, message, Toast.LENGTH_LONG).show()        } else {            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {                ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE)            }        }    }    fun requestSpecificPermissions(permissions: Array<String>, message: Int, permissionCode: Int) {        val size = permissions.size        var isShouldShowRequestPermissionRationale = false        for (index in 0 until size) {            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[index])) {                isShouldShowRequestPermissionRationale = true                break            }        }        if (isShouldShowRequestPermissionRationale) {            Toast.makeText(this, message, Toast.LENGTH_LONG).show()        } else {            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {                ActivityCompat.requestPermissions(this, permissions, permissionCode)            }        }    }    fun openSettings() {        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)        val uri = Uri.fromParts("package", packageName, null)        intent.data = uri        startActivity(intent)    }}