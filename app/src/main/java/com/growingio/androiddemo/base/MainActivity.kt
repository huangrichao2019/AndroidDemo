package com.growingio.androiddemo.base

import android.annotation.TargetApi
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import com.google.android.material.internal.NavigationMenuView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.integration.android.IntentIntegrator
import com.growingio.androiddemo.R
import com.growingio.androiddemo.apiconfig.ShowAPIFragment
import com.growingio.androiddemo.application.MyApplication
import com.growingio.androiddemo.checklist.CheckListFragment
import com.growingio.androiddemo.hybridsdk.HybridSDKPresentationFragment
import com.growingio.androiddemo.manualtrack.ManualTrackFragment
import com.growingio.androiddemo.questions.QuestionsFragment
import com.growingio.androiddemo.showfeature.ShowFeaturesFragment
import com.growingio.androiddemo.userstory.UserStoryFragment
import kotlinx.android.synthetic.main.activity_drawer.*
import kotlinx.android.synthetic.main.app_bar_drawer.*
import org.json.JSONArray
import org.json.JSONObject


/**
 * classDesc: 首页
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, RadioGroup.OnCheckedChangeListener {
    private val showFeatures = ShowFeaturesFragment()
    private val showAPIFragment = ShowAPIFragment()
    private val checkListFragment = CheckListFragment()
    private val userStoryFragment = UserStoryFragment()
    private val manualTrackFragment = ManualTrackFragment()
    private val hybridSDKPresentationFragment = HybridSDKPresentationFragment()
    private val questionsFragment = QuestionsFragment()
    private var popWindow: PopupWindow? = null
    private var screenWidth = 0
    private var screenHeight = 0
    private var snackbar: Snackbar? = null
    private var app: MyApplication? = null
    private var collectData: TextView? = null
    private var container: RadioGroup? = null
    private var dataView: View? = null
    private var btn_switch: SwitchCompat? = null
    private var isSwitchChecked = false
    private var list: MutableList<JSONObject>? = null


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer)
        setSupportActionBar(bar)
        app = application as MyApplication

        supportFragmentManager.beginTransaction().add(R.id.container, showFeatures).commit()


        fab.setOnClickListener { view ->
            if (snackbar != null && snackbar!!.isShown) {
                snackbar!!.dismiss()
            } else {
                initSnackBar()
                snackbar!!.show()
            }
        }


        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, bar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        val navMenu = nav_view.getChildAt(0) as NavigationMenuView
        navMenu.isVerticalScrollBarEnabled = false
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun initSnackBar() {
        list = app!!.listMessage
        if (list!!.isEmpty()) return

        snackbar = null
        snackbar = Snackbar.make(fab, "", Snackbar.LENGTH_INDEFINITE)
        val layout = snackbar!!.view as Snackbar.SnackbarLayout

        snackbar!!.view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                snackbar!!.view.viewTreeObserver.removeOnPreDrawListener(this)
                (snackbar!!.view.layoutParams as CoordinatorLayout.LayoutParams).behavior = null
                return true
            }
        })

        layout.removeAllViews()
        dataView = LayoutInflater.from(this).inflate(R.layout.layout_show_collect_data, null, false)

        collectData = dataView!!.findViewById<View>(R.id.collect_data) as TextView
        btn_switch = dataView!!.findViewById<View>(R.id.btn_switch) as SwitchCompat
        val btnClear = dataView!!.findViewById<View>(R.id.iv_clear) as ImageView
        container = dataView!!.findViewById<View>(R.id.event_container) as RadioGroup


        btnClear.setOnClickListener {
            app!!.listMessage.clear()
            container!!.removeAllViews()
            collectData!!.text = ""
        }

        btn_switch!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (list!!.size > 0) {

                if (isChecked) {
                    isSwitchChecked = true
                    setMessageTypeRadioButtons(list!!)
                    collectData!!.text = format(translateMessageType(list!!.last()))
                } else {
                    isSwitchChecked = false
                    setMessageTypeRadioButtons(list!!)
                    collectData!!.text = format(list!!.last())
                }
            }
        }

        setMessageTypeRadioButtons(list!!)


        layout.addView(dataView)

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun setMessageTypeRadioButtons(list: MutableList<JSONObject>) {

        container!!.removeAllViews()

        for (message in list) {
            collectData!!.text = format(message)
            var eventType = RadioButton(this)
            val params = LinearLayout.LayoutParams(280, 135)
            params.setMargins(0, 0, 0, 10)
            eventType.gravity = Gravity.CENTER
            eventType.layoutParams = params
            eventType.setPadding(30, 0, 30, 0)
            eventType.setTextColor(Color.WHITE)
            if (isSwitchChecked && app!!.meaningMap[message.optString("t")] != null) {
                eventType.text = app!!.meaningMap[message.optString("t")]
            } else {
                eventType.text = message.optString("t")
            }
            eventType.buttonDrawable = null
            eventType.background = resources.getDrawable(R.drawable.selector_radio_button)
            container!!.addView(eventType)
        }

        container!!.setOnCheckedChangeListener(this)
        (container!!.getChildAt(list!!.size - 1) as RadioButton).isChecked = true
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

        if (isSwitchChecked) {
            collectData!!.text = format(translateMessageType(list!![getCheckedPosition(group!!)]))
        } else {
            collectData!!.text = format(list!![getCheckedPosition(group!!)])
        }
    }

    fun getCheckedPosition(radioGroup: RadioGroup): Int {
        return radioGroup.indexOfChild(dataView!!.findViewById(radioGroup.checkedRadioButtonId))
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        screenWidth = windowManager.defaultDisplay.width
        screenHeight = windowManager.defaultDisplay.height
        popWindow = PopupWindow(screenWidth, screenHeight - 300)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else if (snackbar != null && snackbar!!.isShown) {
            snackbar!!.dismiss()
        } else {
            super.onBackPressed()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.drawer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.menu_scan) {
            val integrator = IntentIntegrator(this)
            integrator.setOrientationLocked(false)
            integrator.captureActivity = CustomCaptureActivity::class.java
            integrator.initiateScan()
            true
        } else super.onOptionsItemSelected(item)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.nav_show_feature) {
            supportFragmentManager.beginTransaction().replace(R.id.container, showFeatures).commit()
        } else if (id == R.id.nav_config_api) {
            supportFragmentManager.beginTransaction().replace(R.id.container, showAPIFragment).commit()
        } else if (id == R.id.nav_manual_track) {
            supportFragmentManager.beginTransaction().replace(R.id.container, manualTrackFragment).commit()
        } else if (id == R.id.nav_hybrid_sdk) {
            supportFragmentManager.beginTransaction().replace(R.id.container, hybridSDKPresentationFragment).commit()
        } else if (id == R.id.nav_user_story) {
            supportFragmentManager.beginTransaction().replace(R.id.container, userStoryFragment).commit()
        } else if (id == R.id.nav_check_list) {
            supportFragmentManager.beginTransaction().replace(R.id.container, checkListFragment).commit()
        } else if (id == R.id.nav_questions) {
            supportFragmentManager.beginTransaction().replace(R.id.container, questionsFragment).commit()
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val mClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("gio_scan", result.contents)
                mClipboardManager.primaryClip = clipData
                startActivity(Intent(this, StandardWebView::class.java).putExtra("url", result.contents))
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun getLevelStr(level: Int): String {
        val levelStr = StringBuilder()
        for (levelI in 0 until level) {
            levelStr.append("\t")
        }
        return levelStr.toString()
    }

    private fun translateMessageType(jsonOriginal: JSONObject): JSONObject {
        var newJSONObject = JSONObject()
        var newElementArray = JSONArray()
        for (key in jsonOriginal.keys()) {

            //element
            if (key.equals("e")) {
                var elementArray = jsonOriginal.optJSONArray("e")
                for (i in 0 until elementArray.length()) {
                    var elementJSONObject = elementArray[i] as JSONObject
                    var newElementJSONObject = JSONObject()
                    for (elementKey in elementJSONObject.keys()) {
                        if (app!!.meaningMap[elementKey] != null) {
                            newElementJSONObject.put(app!!.meaningMap[elementKey], elementJSONObject.optString(elementKey))
                        } else {
                            newElementJSONObject.put(elementKey, elementJSONObject.optString(elementKey))
                        }
                    }
                    newElementArray.put(i, newElementJSONObject)
                }
                newJSONObject.put("元素", newElementArray)
            } else {
                if (app!!.meaningMap[key] != null) {
                    newJSONObject.put(app!!.meaningMap[key], jsonOriginal.optString(key))
                } else {
                    newJSONObject.put(key, jsonOriginal.optString(key))
                }
            }

        }

        return newJSONObject
    }

    fun format(jsonOriginal: JSONObject): String {
        var level = 0
        val jsonForMatStr = StringBuilder()
        val data = jsonOriginal.toString()
        for (i in 0 until data.length) {
            val c = data[i]
            if (level > 0 && '\n' == jsonForMatStr[jsonForMatStr.length - 1]) {
                jsonForMatStr.append(getLevelStr(level))
            }
            when (c) {
                '{' -> {
                    jsonForMatStr.append(c).append("\n")
                    level++
                }
                ',' -> jsonForMatStr.append(c).append("\n")
                '}' -> {
                    jsonForMatStr.append("\n")
                    level--
                    jsonForMatStr.append(getLevelStr(level))
                    jsonForMatStr.append(c)
                }
                else -> jsonForMatStr.append(c)
            }
        }

        return jsonForMatStr.toString()

    }
}
