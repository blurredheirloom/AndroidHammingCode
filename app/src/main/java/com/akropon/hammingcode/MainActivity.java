package com.akropon.hammingcode;

import android.content.Intent;

import android.graphics.Typeface;
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
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText editText_input;
    ImageButton btn_left;
    ImageButton btn_right;
    ImageButton btn_backspace;
    Button btn_addone;
    Button btn_addzero;
    ImageButton btn_deleteall;
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
        disableButtons(editText_input.length()>0);
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
     * Присоединяет прослушиватели к элементам активити
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
                    disableButtons(editText_input.length()>0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                formatInputView();
            }
        });
    }

    private void disableButtons(boolean input)
    {
        btn_decode.setEnabled(input);
        btn_encode.setEnabled(input);
        btn_left.setEnabled(input);
        btn_right.setEnabled(input);
        btn_backspace.setEnabled(input);
        btn_deleteall.setEnabled(input);
        btn_left.setAlpha(input ? 1.0f : 0.3f);
        btn_right.setAlpha(input ? 1.0f : 0.3f);
        btn_backspace.setAlpha(input ? 1.0f : 0.3f);
        btn_deleteall.setAlpha(input ? 1.0f : 0.3f);
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
            Toast.makeText(this, getString(R.string.confirm_delete), Toast.LENGTH_SHORT).show();
            previousTimeOfPressingDeleteallBtn = time;
        }
    }


    protected void onClick_encode_decode(boolean isEncode) {
        if(editText_input.getText().toString().length()>0)
        {
            Intent resultActivityIntent = new Intent(MainActivity.this,
                    ResultActivity.class);
            resultActivityIntent.putExtra("isEncode", isEncode);
            resultActivityIntent.putExtra("inputString", editText_input.getText().toString());
            startActivity(resultActivityIntent);
            overridePendingTransition(R.anim.activity_result_animation_appearance,
                    R.anim.activity_main_animation_hiding);
        }
    }


    // функция, форматируюая текст в текстовом поле ввода после его изменения
    protected void formatInputView() {
        if (stopFlagForTextWatcher) {
            stopFlagForTextWatcher = false;
            return;
        }
        stopFlagForTextWatcher = true;

        int selectionStart = editText_input.getSelectionStart();
        int selectionEnd = editText_input.getSelectionEnd();

        // Вырезаем из строки все символы, кроме "0" и "1"
        StringBuilder validStr = new StringBuilder("");
        char symbol;
        for (int i=0; i<editText_input.getText().length(); i++) {
            symbol = editText_input.getText().charAt(i);
            if (symbol == '0' || symbol == '1') {
                validStr.append(symbol);
            }
        }

        // создаем контейнер для Rich-текстового представления исправленной строки
        Spannable richText = new SpannableString(validStr);

        // исправляем границы выделения, т.к. после исправления они могут "поплыть"
        if (selectionEnd > richText.length())
            selectionEnd = richText.length();
        if (selectionStart > selectionEnd)
            selectionStart = selectionEnd;

        int quadroBlocksAmount = richText.length() / 4;
        boolean bold = true;
        for (int i=0; i<quadroBlocksAmount; i++) {
            richText.setSpan(new android.text.style.StyleSpan(bold ? Typeface.BOLD: Typeface.NORMAL),
                    i*4, i*4+4,  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            bold = !bold;
        }

        /*if (richText.length() % 4 != 0) {
            richText.setSpan(new ForegroundColorSpan(grey ? Color.GRAY : Color.BLACK),
                    quadroBlocksAmount*4, richText.length(),  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            //grey = !grey;
        }*/

        editText_input.setText(richText);
        editText_input.setSelection(selectionStart, selectionEnd);
    }

}
