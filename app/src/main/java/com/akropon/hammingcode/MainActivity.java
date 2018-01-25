package com.akropon.hammingcode;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText editText_input;
    Button btn_left;
    Button btn_right;
    Button btn_backspace;
    Button btn_addone;
    Button btn_addzero;
    Button btn_deleteall;
    Button btn_encode;
    Button btn_decode;

    long previousTimeOfPressingDeleteallBtn = 0;
    boolean stopFlagForTextWatcher; // останавливает самовызов обработаки текствотчера

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findElements();
        setListenersToElements();
        elementsStartCustomization();

    }

    /**
     * Привязывает элементы активити к переменным данного класса
     */
    protected void findElements() {
        editText_input = findViewById(R.id.edittext_input);
        btn_left = findViewById(R.id.btn_left);
        btn_right = findViewById(R.id.btn_right);
        btn_backspace = findViewById(R.id.btn_backspace);
        btn_addone = findViewById(R.id.btn_addone);
        btn_addzero = findViewById(R.id.btn_addzero);
        btn_deleteall = findViewById(R.id.btn_deleteall);
        btn_encode = findViewById(R.id.btn_encode);
        btn_decode = findViewById(R.id.btn_decode);


    }

    /**
     * Присоединяет прослушиватели нажатий к элементам активити
     */
    protected void setListenersToElements() {

        btn_addone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_addone();
            }
        });

        btn_addzero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_addzero();
            }
        });

        btn_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_left();
            }
        });

        btn_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_right();
            }
        });

        btn_backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_backspace();
            }
        });

        btn_deleteall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_deleteall();
            }
        });

        btn_encode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_encode_decode(true);
            }
        });

        btn_decode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_encode_decode(false);
            }
        });

        editText_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                formatInputView();
            }
        });
    }

    /**
     * Преднастройка элементов активити
     */
    protected void elementsStartCustomization() {
        editText_input.setShowSoftInputOnFocus(false);

        previousTimeOfPressingDeleteallBtn = 0;
        stopFlagForTextWatcher = false;
    }

    protected void onClick_addone() {
        // todo проверка на область выделения (selection), т.к. от нее зависит положение "курсора"
        insertInEditTextOfInput('1');
    }

    protected void onClick_addzero() {
        // todo проверка на область выделения (selection), т.к. от нее зависит положение "курсора"
        insertInEditTextOfInput('0');
    }

    protected void onClick_right() {
        editText_input.setSelection(editText_input.getSelectionEnd());
        if (editText_input.getSelectionEnd() != editText_input.length())
            editText_input.setSelection(editText_input.getSelectionEnd()+1);
    }

    protected void onClick_left() {
        editText_input.setSelection(editText_input.getSelectionStart());
        if (editText_input.getSelectionEnd() != 0)
            editText_input.setSelection(editText_input.getSelectionEnd()-1);
    }

    protected void insertInEditTextOfInput(char _char) {
        editText_input.getText().replace(
                editText_input.getSelectionStart(), editText_input.getSelectionEnd(),
                String.valueOf(_char), 0, 1);
        editText_input.setSelection(editText_input.getSelectionEnd());
    }

    protected void onClick_backspace() {
        int sel_st = editText_input.getSelectionStart();
        int sel_en = editText_input.getSelectionEnd();
        if (sel_en != sel_st) {
            editText_input.getText().delete(sel_st, sel_en);
        } else {
            if (sel_en != 0)
                editText_input.getText().delete(sel_en-1, sel_en);
        }
    }

    protected void onClick_deleteall() {
        long time = System.currentTimeMillis();
        if (time - previousTimeOfPressingDeleteallBtn < Cnst.timeoutForDoubleClickingDeleteallBtn) {
            editText_input.setText("");
            previousTimeOfPressingDeleteallBtn = 0; //перестраховка
        } else {
            Toast.makeText(this, Cnst.doubleClickingDeleteallBtnMsg, Toast.LENGTH_SHORT).show();
            previousTimeOfPressingDeleteallBtn = time;
        }
    }


    protected void onClick_encode_decode(boolean isEncode) {
        Intent resultActivityIntent = new Intent(MainActivity.this,
                ResultActivity.class);
        resultActivityIntent.putExtra("isEncode", isEncode);
        resultActivityIntent.putExtra("inputString", editText_input.getText().toString());
        startActivity(resultActivityIntent);
        overridePendingTransition(R.anim.activity_result_animation_appearance,
                R.anim.activity_main_animation_hiding);
    }

    protected void formatInputView() {
        if (stopFlagForTextWatcher) {
            stopFlagForTextWatcher = false;
            return;
        }
        stopFlagForTextWatcher = true;

        int selectionStart = editText_input.getSelectionStart();
        int selectionEnd = editText_input.getSelectionEnd();
        Spannable richText = new SpannableString(editText_input.getText().toString());

        int quadroBlocksAmount = richText.length() / 4;
        boolean grey = false;
        for (int i=0; i<quadroBlocksAmount; i++) {
            richText.setSpan(new ForegroundColorSpan(grey ? Color.GRAY : Color.BLACK),
                    i*4, i*4+4,  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            grey = !grey;
        }

        if (richText.length() % 4 != 0) {
            richText.setSpan(new ForegroundColorSpan(grey ? Color.GRAY : Color.BLACK),
                    quadroBlocksAmount*4, richText.length(),  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            //grey = !grey;
        }

        editText_input.setText(richText);
        editText_input.setSelection(selectionStart, selectionEnd);
    }

}