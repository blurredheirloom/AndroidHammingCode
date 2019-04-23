package com.akropon.hammingcode;

import java.lang.Math;
import java.util.Arrays;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ResultActivity extends AppCompatActivity {
    TextView textview_result;
    ImageButton btn_back;
    ImageButton btn_copyAll;
    ImageButton btn_copyOnlyOutWord;

    String inStr;
    boolean isEncode;

    String outWord;         // только выходное слово (заполняется по созданию активити)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        findElements();
        setListenersToElements();
        startCustomization();
    }

    /**
     * Привязывает элементы активити к переменным данного класса
     */
    protected void findElements() {
        textview_result = findViewById(R.id.textview_result);
        btn_back = findViewById(R.id.btn_back);
        btn_copyAll = findViewById(R.id.btn_copy_all);
        btn_copyOnlyOutWord = findViewById(R.id.btn_copy_result_word);
    }

    /**
     * Присоединяет прослушиватели нажатий к элементам активити
     */
    protected void setListenersToElements() {

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_back();
            }
        });

        btn_copyAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_copyAll();
            }
        });

        btn_copyOnlyOutWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_copyOnlyOutWord();
            }
        });
    }

    /**
     * Преднастройка элементов активити
     */
    protected void startCustomization() {
        isEncode = getIntent().getBooleanExtra("isEncode", true);
        inStr = getIntent().getStringExtra("inputString");

        outWord = Cnst.undefinedOutputFieldFiller;

        textview_result.setText("");

        if (isEncode)
            launchEncoding();
        else
            launchDecoding();
    }


    protected void onClick_back() {
        overridePendingTransition(R.anim.activity_result_animation_disappearance,
                R.anim.activity_main_animation_showing);
        finish();
    }

    protected void onClick_copyAll() {
        try {
            ClipboardManager clipboard =
                    (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", textview_result.getText().toString());
            clipboard.setPrimaryClip(clip);
        } catch (Exception exc) {
            Toast.makeText(this, getString(R.string.error_copy),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, getString(R.string.copy_all), Toast.LENGTH_SHORT).show();
    }

    protected void onClick_copyOnlyOutWord() {
        try {
            ClipboardManager clipboard =
                    (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", outWord);
            clipboard.setPrimaryClip(clip);
        } catch (Exception exc) {
            Toast.makeText(this, getString(R.string.error_copy),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, getString(R.string.copy_word), Toast.LENGTH_SHORT).show();
    }

    protected void launchEncoding() {
        StringBuilder outStr = new StringBuilder();
        outStr.append(getString(R.string.original_word)+":");
        outStr.append("\n  A = "+inStr);
        outStr.append("\n\n"+getString(R.string.original_word_length));
        outStr.append("\n |A| = "+inStr.length());

        // Вычислим кол-во контрольных битов по формуле ниже.
        // kBitsAmount = roundDown[log2(|a|)]+1 = roundDown[log(|a|)/log(2)]+1
        // Из математических соображений, эта формула даст либо верный результат, либо
        // результат, который на единицу меньше валидного. Поэтому после вычисления
        // последует проверка, и при необходимости добавится недостающая единица.
        int kBitsAmount = (int)(Math.floor( Math.log(inStr.length())/Math.log(2)) ) + 1;
        if ( Math.pow( 2, kBitsAmount )-1 < inStr.length()+kBitsAmount )
            kBitsAmount++;

        // Массив позиций контрольных битов (с инициализацией)
        int[] kBitsPositions = new int[kBitsAmount];
        int pos = 1;
        for (int i=0; i<kBitsAmount; i++) {
            kBitsPositions[i] = pos - 1;
            pos = pos << 1; // умножение на 2
        }

        /* Генерируем строку из неопределенных контрольных битов и входного слова */
        char[] strWithUndefKBits = new char[inStr.length()+kBitsAmount];
        int s=0;
        int t=0;
        for (int p=0; p<strWithUndefKBits.length; p++) {
            if (s<kBitsAmount) {
                if (p==kBitsPositions[s]) {
                    strWithUndefKBits[p] = 'x';
                    s++;
                    continue;
                }
            }

            strWithUndefKBits[p] = inStr.charAt(t);
            t++;
        }

        outStr.append("\n\n"+getString(R.string.control_bit)+":");
        outStr.append("\n  b = ").append(strWithUndefKBits);

        // слово с нулевыми контрольными битами
        boolean[] arrWithUndefKBits = new boolean[strWithUndefKBits.length];
        for (int i=0; i<arrWithUndefKBits.length; i++)
            arrWithUndefKBits[i] = strWithUndefKBits[i] == '1';
        // "матрица контроля"
        boolean[][] kMatrix = new boolean[kBitsAmount][arrWithUndefKBits.length];
        for (int i=0; i<kBitsAmount; i++) {
            boolean isControlSeries = true;
            int counter=0;
            for (int j=kBitsPositions[i]; j<strWithUndefKBits.length; j++) {
                kMatrix[i][j] = isControlSeries;
                counter++;
                if (counter > kBitsPositions[i]) {
                    isControlSeries = !(isControlSeries);
                    counter=0;
                }
            }
        }

        outStr.append("\n\n"+getString(R.string.control_matrix)+"\n"+getString(R.string.matrix_description)+":");
        outStr.append("\n\t\t\t\t").append(strWithUndefKBits);
        for (int i=0; i<kBitsAmount; i++) {
            outStr.append("\nk").append(i+1);
            if (i<10)
                outStr.append("\t\t");
            for (int j=0; j<strWithUndefKBits.length; j++)
                outStr.append(kMatrix[i][j] ? '+' : '.');

        }

        // Массив контрольных битов (с инициализацией нулями)
        boolean[] kBits = new boolean[kBitsAmount];
        Arrays.fill(kBits, false);
        // Вычисление контрольных битов по "матрице контроля"
        for (int i=0; i<kBitsAmount; i++)
            for (int j=kBitsPositions[i]; j<arrWithUndefKBits.length; j++)
                if (kMatrix[i][j])
                    kBits[i] = kBits[i] ^ arrWithUndefKBits[j];


        outStr.append("\n\n"+getString(R.string.control_bit_values)+":");
        for (int i=0; i<kBitsAmount; i++) {
            outStr.append("\nk").append(i + 1).append(" = ").append(kBits[i] ? '1' : '0');
        }

        // Генерируем закодированное слово
        for (int i=0; i<kBitsAmount; i++)
            strWithUndefKBits[kBitsPositions[i]] = kBits[i] ? '1' : '0';

        outStr.append("\n\n\n"+getString(R.string.encoded_word)+":");
        outStr.append("\nB = ").append(strWithUndefKBits);

        outStr.append("\n\n\n\n\n\n");  // чтобы прокрутить можно было подальше
        textview_result.append(outStr.toString());
        outWord = String.valueOf(strWithUndefKBits);
    }

    protected void launchDecoding() {
        StringBuilder outStr = new StringBuilder();
        outStr.append(getString(R.string.encoded_word)+getString(R.string.original)+":");
        outStr.append("\nB = "+inStr);
        outStr.append("\n\n"+getString(R.string.original_word_length)+":");
        outStr.append("\n|B| = "+inStr.length());

        // Вычислим кол-во контрольных битов по формуле ниже.
        // kBitsAmount = roundDown[log2(|a|)]+1 = roundDown[log(|a|)/log(2)]+1
        int kBitsAmount = (int)(Math.floor( Math.log(inStr.length())/Math.log(2)) ) + 1;

        // Массив позиций контрольных битов (с инициализацией)
        int[] kBitsPositions = new int[kBitsAmount];
        int pos = 1;
        for (int i=0; i<kBitsAmount; i++) {
            kBitsPositions[i] = pos - 1;
            pos = pos << 1; // умножение на 2
        }

        // То же исходное слово, только со сброшенными контрольными битами
        /*char[] strWithUndefKBits = inStr.toCharArray();
        for (int i=0; i<kBitsAmount; i++)
            strWithUndefKBits[kBitsPositions[i]] = 'x';
        */

        outStr.append("\n\n"+getString(R.string.bits_position)+":");
        outStr.append("\n").append(inStr).append("\n");
        int s = 0;
        for (int i=0; i<inStr.length(); i++) {
            if (s<kBitsAmount) {
                if (i == kBitsPositions[s]) {
                    outStr.append('^');
                    s++;
                    continue;
                }
            }
            outStr.append(' ');
        }

        //outStr.append("Далее выполняем пересчет контрольных битов, основываясь");
        //outStr.append("на информационной составляющей исходного слова.");

        // входное слово в виде булева массива
        boolean[] inArr = new boolean[inStr.length()];
        for (int i=0; i<inArr.length; i++)
            inArr[i] = (inStr.charAt(i) == '1');

        // "матрица синдромов"
        boolean[][] sMatrix = new boolean[kBitsAmount][inArr.length];
        for (int i=0; i<kBitsAmount; i++) {
            boolean isControlSeries = true;
            int counter=0;
            for (int j=kBitsPositions[i]; j<inArr.length; j++) {
                sMatrix[i][j] = isControlSeries;
                counter++;
                if (counter > kBitsPositions[i]) {
                    isControlSeries = !(isControlSeries);
                    counter=0;
                }
            }
        }

        outStr.append("\n\n"+getString(R.string.matrix_errors)+":");
        outStr.append("\n\t\t\t").append(inStr);
        for (int i=0; i<kBitsAmount; i++) {
            outStr.append("\ns").append(i+1);
            if (i<10)
                outStr.append("\t");
            for (int j=0; j<inArr.length; j++)
                outStr.append(sMatrix[i][j] ? '+' : '.');

        }

        // Массив синдромов (с инициализацией нулями)
        boolean[] syndromes = new boolean[kBitsAmount];
        Arrays.fill(syndromes, false);
        // Вычисление синдромов по "матрице синдромов"
        for (int i=0; i<kBitsAmount; i++)
            for (int j=kBitsPositions[i]; j<inArr.length; j++)
                if (sMatrix[i][j])
                    syndromes[i] = syndromes[i] ^ inArr[j];


        outStr.append("\n\n"+getString(R.string.errors_meaning)+":");
        for (int i=0; i<kBitsAmount; i++) {
            outStr.append("\ns").append(i + 1).append(" = ").append(syndromes[i] ? '1' : '0');
        }

        outStr.append("\n\n"+getString(R.string.binary_errors)+":");
        outStr.append("\nS = ");
        for (int i=syndromes.length-1; i>=0; i--) {
            outStr.append(syndromes[i] ? '1' : '0');
        }

        // Проверяем, есть ли ошибка в закодированном слове
        boolean noErr = true;
        for (int i=0; i<kBitsAmount; i++) {
            if (syndromes[i]) {
                noErr = false;
                break;
            }
        }

        // Переменная, утверждающая о наличии более одной ошибки
        boolean isMoreThan1Err = false;

        // Исправляем ошибку по необходимости
        if (noErr) {
            // noErr == true
            outStr.append("\n\n"+getString(R.string.no_errors));
            outStr.append("\n"+getString(R.string.no_correction));


        } else {
            // noErr == false
            outStr.append("\n\n"+getString(R.string.errors));
            outStr.append("\n"+getString(R.string.error_position));

            // Вычисляем позицию ошибки
            int errPos = 0;
            int var = 1;
            for (int i=0; i<kBitsAmount; i++) {
                if (syndromes[i]) {
                    errPos += var;
                }
                var = var<<1;
            }
            errPos--;

            outStr.append("\n  S = ").append(errPos+1).append(" "+getString(R.string.decimal));

            // Проверка на наличие более одной ошибки
            if (errPos >= inArr.length) {
                isMoreThan1Err = true;
            } else {
                outStr.append("\n\n"+getString(R.string.error_position_original)+":");
                outStr.append("\n").append(inStr);
                outStr.append("\n");
                for (int i = 0; i < inStr.length(); i++)
                    outStr.append(i == errPos ? '^' : ' ');

                // Исправляем ошибку в inArr
                inArr[errPos] = !inArr[errPos];

                outStr.append("\n\n"+getString(R.string.correct_word)+":\n");
                for (int i = 0; i < inArr.length; i++)
                    outStr.append(inArr[i] ? '1' : '0');
            }
        }

        if (isMoreThan1Err) {
            outStr.append("\n\nS > |B|, "+ getString(R.string.two_errors));
            outStr.append("\n"+ getString(R.string.impossible_correction));
        } else {
            // На данный момент inArr считается не имеющим ошибок
            // Генерируем информационное (расшифрованное) слово
            char[] decodedWord = new char[inArr.length - kBitsAmount];
            int p = 0;
            int t = 0;
            for (int i = 0; i < inArr.length; i++) {
                if (p < kBitsAmount) {
                    if (i == kBitsPositions[p]) {
                        p++;
                        continue;
                    }
                }
                decodedWord[t] = inArr[i] ? '1' : '0';
                t++;
            }

            outStr.append("\n\n"+getString(R.string.remove_control_bits));
            outStr.append("\n"+getString(R.string.decoded_word)+":");
            outStr.append("\n  A = ").append(decodedWord);

            outWord = String.valueOf(decodedWord);
        }

        textview_result.append(outStr.toString());
    }



}
