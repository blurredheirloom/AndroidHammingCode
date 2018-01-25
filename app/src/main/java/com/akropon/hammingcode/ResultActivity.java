package com.akropon.hammingcode;

import java.lang.Math;
import java.util.Arrays;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {
    TextView textview_result;
    Button btn_back;

    String inStr;
    boolean isEncode;

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
    }

    /**
     * Преднастройка элементов активити
     */
    protected void startCustomization() {
        isEncode = getIntent().getBooleanExtra("isEncode", true);
        inStr = getIntent().getStringExtra("inputString");

        textview_result.setText("");

        if (inStr.length() == 0) {
            textview_result.append("Заполните слово");
            return;
        }

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

    protected void launchEncoding() {
        StringBuilder outStr = new StringBuilder();
        outStr.append("Исходное слово:");
        outStr.append("\na = "+inStr);
        outStr.append("\nДлина исходного слова:");
        outStr.append("\n|a| = "+inStr.length());

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

        outStr.append("\nНачнем расставлять контрольные биты:");
        outStr.append("\nb = ").append(strWithUndefKBits);

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

        outStr.append("\nМатрица контроля (принимаем контрольные биты как 0):");
        outStr.append("\n    ").append(strWithUndefKBits);
        for (int i=0; i<kBitsAmount; i++) {
            outStr.append("\nk").append(i+1);
            if (i<10)
                outStr.append("  ");
            else
                outStr.append(" ");
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


        outStr.append("\nЗначения контрольных битов:");
        for (int i=0; i<kBitsAmount; i++) {
            outStr.append("\nk").append(i + 1).append(" = ").append(kBits[i] ? '1' : '0');
        }

        // Генерируем закодированное слово
        for (int i=0; i<kBitsAmount; i++)
            strWithUndefKBits[kBitsPositions[i]] = kBits[i] ? '1' : '0';

        outStr.append("\nЗакодированное слово:");
        outStr.append("\nb = ").append(strWithUndefKBits);

        outStr.append("\n");
        outStr.append("\n");
        outStr.append("\n");

        textview_result.append(outStr.toString());

        outStr.append("\n");
    }

    protected void launchDecoding() {
        StringBuilder outStr = new StringBuilder();
        outStr.append("Исходное (закодированное) слово:");
        outStr.append("\nb = "+inStr);
        outStr.append("\nДлина исходного слова:");
        outStr.append("\n|b| = "+inStr.length());

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

        outStr.append("\nНиже указаны позиции контрольных битов в исходном слове");
        outStr.append('\n').append(inStr).append('\n');
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

        // "матрица контроля"
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

        outStr.append("\nМатрица синдромов:");
        outStr.append("\n    ").append(inStr);
        for (int i=0; i<kBitsAmount; i++) {
            outStr.append("\ns").append(i+1);
            if (i<10)
                outStr.append("  ");
            else
                outStr.append(" ");
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


        outStr.append("\nЗначения синдромов:");
        for (int i=0; i<kBitsAmount; i++) {
            outStr.append("\ns").append(i + 1).append(" = ").append(syndromes[i] ? '1' : '0');
        }

        outStr.append("\nЗапишем синдромы в виде двоичного числа:");
        outStr.append("\n S = ");
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
            outStr.append("\nТ.к. синдром S нулевой, значит ошибок не обнаружено.");
            outStr.append("\nИсправление не требуется.");


        } else {
            // noErr == false
            outStr.append("\nСиндром S не нулевой. Требуется исправление ошибки.");
            outStr.append("\nЧисло S показывает позицию ошибки.");

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

            outStr.append("\nS = ").append(errPos+1).append("(в десятичной форме)");

            // Проверка на наличие более одной ошибки
            if (errPos >= inArr.length) {
                isMoreThan1Err = true;
            } else {
                outStr.append("\nНиже указана позиция ошибки в исходном слове:");
                outStr.append("\n").append(inStr);
                outStr.append("\n");
                for (int i = 0; i < inStr.length(); i++)
                    outStr.append(i == errPos ? '^' : ' ');

                // Исправляем ошибку в inArr
                inArr[errPos] = !inArr[errPos];

                outStr.append("\nЗакодированное слово с исправленной ошибкой:\n");
                for (int i = 0; i < inArr.length; i++)
                    outStr.append(inArr[i] ? '1' : '0');
            }
        }

        if (isMoreThan1Err) {
            outStr.append("\nS > |b|, следовательно вхоное слово содержит более одной ошибки.");
            outStr.append("\nВосстановление невозможно.");
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

            outStr.append("\nТеперь убираем конрольные биты и получаем");
            outStr.append("\nдекодированное информационное слово:");
            outStr.append("\n").append(decodedWord);
        }


        textview_result.append(outStr.toString());
    }

}