package com.example.trareco

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog


class MyDialogFragment : androidx.fragment.app.DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = activity?.let {
            AlertDialog.Builder(it)
                .setTitle("タイトル")
                .setMessage("ここにメッセージを入力します")
                .setPositiveButton("OK") { dialog, id ->
                    // ここにコードを書きます
                }
                .setNegativeButton("キャンセル") { dialog, id -> }
                .setNeutralButton("あとで") { dialog, id -> }
                .create()
        }

        return dialog ?: throw IllegalStateException("アクティビティがNullです。")
    }
}