package com.example.trareco.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row

@Composable
fun Calendar ()
{
    OneDayCell(3)
}

@Composable
fun OneDayCell (date: Int)
{
    Box(
        modifier = Modifier
            .padding(start = 6.dp, bottom = 6.dp)
            .size(width = 30.dp, height = 30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color = DarkOrange),
        contentAlignment = Alignment.Center
    ){
        Text(date.toString())
    }
}

@Composable
fun OneWeek (date: Int)
{
    Row ()
    {
        for (i in date .. date + 6) {
            OneDayCell(i)
        }
    }
}

@Composable
fun OneMonth ()
{
    val weekDays = arrayOf("日","月","火","水","木","金","土")

    Column {
        Row (modifier = Modifier.padding(bottom = 6.dp))
        {
            for (i in 0..6) {
                Box(
                    modifier = Modifier
                        .padding(start = 6.dp, bottom = 6.dp)
                        .size(width = 30.dp, height = 30.dp),
                    contentAlignment = Alignment.Center
                ){
                    Text(weekDays[i])
                }
            }
        }
        Column ()
        {
            for (date in 1..30 step 5) {
                OneWeek(date)
            }
        }
    }
}