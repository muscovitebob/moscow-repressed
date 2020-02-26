import $ivy.`org.jsoup:jsoup:1.12.1`, org.jsoup._
import $ivy.`com.lihaoyi:requests_2.12:0.2.0`
import $ivy.`com.lihaoyi::upickle:0.8.0`
import requests._
import upickle.default._

val pageRange = 1 to 78

val allReqs = pageRange.map(num => s"https://mos.memo.ru/shot-$num.htm")
val fullDatabase = allReqs.map(req => Jsoup.connect(req).execute().bufferUp().parse())

import collection.JavaConverters._

val streetList = fullDatabase.map(_.select(".Street").asScala).reduce(_ ++ _)
val tableList = fullDatabase.map(_.select("table").asScala).reduce(_ ++ _)
val streetAndTable = streetList.zip(tableList)
val streetAndPeople = streetAndTable.map{case (street, table) => (street.text, table.select(".pers").asScala.map(_.text))}

val shotDateRegex = """.*(\d{1,2}.\d{1,2}.\d{4}).*""".r
val format = new java.text.SimpleDateFormat("dd.MM.yyyy")

val streetAndPeopleAndDate = streetAndPeople.map{
	case (street, people) => (street, people.map(
		person =>
			(person, person match {
				case shotDateRegex(date) => format.parse(date)
			}
			)
	)
	)
}

// repressedByYear = dates1.map(_.getYear()).groupBy(x => x).map{case (k,v) => (k, v.size)}
// Result: Map(42 -> 101, 24 -> 14, 37 -> 3764, 25 -> 21, 52 -> 20, 29 -> 18, 28 -> 8, 38 -> 6211, 21 -> 1, 33 -> 102, 53 -> 1, 41 -> 206, 32 -> 17, 34 -> 96, 45 -> 5, 22 -> 1, 44 -> 11, 27 -> 29, 39 -> 438, 35 -> 11, 50 -> 31, 31 -> 108, 43 -> 19, 40 -> 182, 26 -> 12, 23 -> 8, 36 -> 187, 30 -> 161, 51 -> 3, 47 -> 3)
// The years 37-38 correspond to the peak of repression. In total 9975 people exterminated in those two years.

val yezhovschina = streetAndPeopleAndDate.map(x => x._2.filter(y => List(37, 38).contains(y._2.getYear)))
