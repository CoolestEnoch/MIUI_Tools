package com.coolest.toolbox.utils

import java.text.ParsePosition
import java.text.SimpleDateFormat

/**
 * Timestamp to String
 * @param Timestamp
 * @return String
 */
public fun transToString(time: Long): String {
	return SimpleDateFormat("YYYY年MM月DD日 hh时mm分ss秒").format(time)
}

/**
 * String to Timestamp
 * @param String
 * @return Timestamp
 */

public fun transToTimeStamp(date: String): Long {
	//2022-03-19T15:03:55Z
	return SimpleDateFormat("YYYY-MM-DD'T'hh:mm:ss'Z'").parse(date, ParsePosition(0)).time
}
