package vn.edu.hust.fileexample

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import vn.edu.hust.fileexample.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
  lateinit var binding: ActivityMainBinding
  var color = Color.TRANSPARENT

  lateinit var db: SQLiteDatabase

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.main)

    val prefs = getSharedPreferences("my_settings", MODE_PRIVATE)
    color = prefs.getInt("background_color", Color.TRANSPARENT)
    binding.main.setBackgroundColor(color)

    binding.radioRed.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        color = Color.RED
        binding.main.setBackgroundColor(color)
      }
    }

    binding.radioGreen.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        color = Color.GREEN
        binding.main.setBackgroundColor(color)
      }
    }

    binding.radioBlue.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        color = Color.BLUE
        binding.main.setBackgroundColor(color)
      }
    }

    val inputStream = resources.openRawResource(R.raw.test)
    val reader = inputStream.reader()
    val content = reader.readText()
    reader.close()

    binding.textContent.text = content

    binding.buttonSave.setOnClickListener {
      val outputStream = openFileOutput("my_data.txt", MODE_PRIVATE)
      val writer = outputStream.writer()
      writer.write(binding.editText.text.toString())
      writer.close()
    }

    binding.buttonLoad.setOnClickListener {
      val inputStream = openFileInput("my_data.txt")
      val reader = inputStream.reader()
      val content = reader.readText()
      reader.close()

      binding.editText.setText(content)
    }

    val sdPath = Environment.getExternalStorageDirectory().path
    Log.v("TAG", sdPath)

    binding.buttonSaveExternal.setOnClickListener {
      val path = Environment.getExternalStorageDirectory().path + "/mydata.txt"
      val file = File(path)
      val outputStream = file.outputStream()
      val writer = outputStream.writer()
      writer.write(binding.editText.text.toString())
      writer.close()
    }

    binding.buttonLoadExternal.setOnClickListener {
      val path = Environment.getExternalStorageDirectory().path + "/mydata.txt"
      val file = File(path)
      val inputStream = file.inputStream()
      val reader = inputStream.reader()
      val content = reader.readText()
      reader.close()

      binding.editText.setText(content)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
      if (!Environment.isExternalStorageManager()) {
        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        startActivity(intent)
      }

    db = SQLiteDatabase.openDatabase(filesDir.path + "/mydb", null,
      SQLiteDatabase.CREATE_IF_NECESSARY)

    // createTable()

    binding.buttonInsert.setOnClickListener {
      db.beginTransaction()
      try {
        val name = binding.editName.text.toString()
        val phone = binding.editPhone.text.toString()
        db.execSQL("insert into tblAmigo(name, phone) values('$name','$phone')")
        db.setTransactionSuccessful()
      } catch (ex: Exception) {
        ex.printStackTrace()
      } finally {
        db.endTransaction()
      }
    }

    binding.buttonUpdate.setOnClickListener {
      db.beginTransaction()
      try {
        val name = binding.editName.text.toString()
        db.execSQL("update tblAmigo set name='$name' where recID > 3")
        db.setTransactionSuccessful()
      } catch (ex: Exception) {
        ex.printStackTrace()
      } finally {
        db.endTransaction()
      }
    }

    binding.buttonDelete.setOnClickListener {
      db.beginTransaction()
      try {
        db.execSQL("delete from tblAmigo where recID > 3")
        db.setTransactionSuccessful()
      } catch (ex: Exception) {
        ex.printStackTrace()
      } finally {
        db.endTransaction()
      }
    }

    // Get all records
    //val cs = db.rawQuery("select * from tblAmigo", null)
    val columns = arrayOf("recID", "name", "phone")
    val cs = db.query("tblAmigo", columns,
      null, null, null, null, null)
    Log.v("TAG", "Num records: ${cs.count}")
    cs.moveToFirst()
    do {
      val recID = cs.getInt(0)
      val name = cs.getString(1)
      val phone = cs.getString(2)
      Log.v("TAG", "$recID - $name - $phone")
    } while (cs.moveToNext())
    cs.close()
  }

  fun createTable() {
    db.beginTransaction()
    try {
      db.execSQL("create table tblAmigo(" +
              "recID integer primary key autoincrement," +
              "name text," +
              "phone text)")
      db.execSQL("insert into tblAmigo(name, phone) values('AAA','555-1111')")
      db.execSQL("insert into tblAmigo(name, phone) values('BBB','555-2222')")
      db.execSQL("insert into tblAmigo(name, phone) values('CCC','555-3333')")
      db.setTransactionSuccessful()
    } catch (ex: Exception) {
      ex.printStackTrace()
    } finally {
      db.endTransaction()
    }
  }

  override fun onStop() {
    val prefs = getSharedPreferences("my_settings", MODE_PRIVATE)
    val editor = prefs.edit()
    editor.putInt("background_color", color)
    editor.apply()

    db.close()

    super.onStop()
  }
}