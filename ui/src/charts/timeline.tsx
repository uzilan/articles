import type Props from "../models/props.ts";
import publishedDate from "../Utils.ts";
import {Bar, BarChart, CartesianGrid, Tooltip, XAxis, YAxis} from "recharts";
import {DateTime} from "luxon";

export default function Timeline(props: Props) {

    const sorted = props.articles
        .sort((a, b) => publishedDate(a.published) <= publishedDate(b.published) ? -1 : 1)

    const groupedByMonths = sorted
        .reduce<Record<string, number>>((acc, article) => {
            let date = publishedDate(article.published);
            const yearMonth = date.toFormat("yyyy-LL")
            acc[yearMonth] = (acc[yearMonth] || 0) + 1;
            return acc;
        }, {})

    const data = Object.entries(groupedByMonths).map(d => ({name: d[0], month: d[1]}))

    const sum = (numbers: number[]) =>
        numbers.reduce((sum, current) => sum + current, 0)


    const avg = (numbers: number[]) => {
        const s = sum(numbers)
        return Math.round(s / numbers.length)
    }

    const years = sorted
        .reduce<Record<string, number>>((acc, article) => {
            const year = publishedDate(article.published).year;
            acc[year] = (acc[year] || 0) + 1;
            return acc;
        }, {})

    const MONTH_ORDER = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

    const monthCounts = sorted.reduce<Record<string, number>>((acc, article) => {
        const month = publishedDate(article.published).monthShort ?? "";
        acc[month] = (acc[month] || 0) + 1;
        return acc;
    }, {});

    const sortedMonthCounts = MONTH_ORDER
        .filter(month => month in monthCounts)
        .map(month => ({month, count: monthCounts[month]}));

    return (
        <>
            <h2 style={{margin: '2rem'}}>Timeline</h2>

            <table style={{width: '100%'}}>
                <tbody>
                <tr>
                    <td>
                        <h3>Stories per year: {avg(Object.values(years))}</h3>
                    </td>
                    <td>
                        <h3>Stories per month: {avg(sortedMonthCounts.map(m => m.count))}</h3>
                    </td>
                </tr>
                <tr>
                    <td>
                        <BarChart width={500} height={200} data={Object.entries(years).map(d => ({
                            year: d[0],
                            count: d[1]
                        }))}>
                            <CartesianGrid strokeDasharray="3 3"/>
                            <XAxis dataKey="year"/>
                            <YAxis/>
                            <Tooltip/>
                            <Bar dataKey="count" fill="#8884d8"/>
                        </BarChart>
                    </td>
                    <td>
                        <BarChart width={500} height={200} data={sortedMonthCounts.map(d => ({
                            month: d.month,
                            count: d.count
                        }))}>
                            <CartesianGrid strokeDasharray="3 3"/>
                            <XAxis dataKey="month" interval={0}/>
                            <YAxis/>
                            <Tooltip/>
                            <Bar dataKey="count" fill="#8884d8"/>
                        </BarChart>
                    </td>
                </tr>
                <tr>
                    <td colSpan={2}>
                        <h3>Stories per month and year</h3>
                    </td>
                </tr>
                <tr style={{width:'100%', textAlign:'center'   }}>
                    <td colSpan={2} style={{width:'100%', textAlign:'center'    }} >
                        <BarChart width={1000} height={250} data={data}>
                            <CartesianGrid strokeDasharray="3 3"/>
                            <XAxis dataKey="name" angle={-45}
                                   textAnchor="end"
                                   dy={2}
                                   interval={0}
                                   height={50}
                                   tickFormatter={date =>
                                       DateTime.fromFormat(date, "yyyy-LL").toFormat('MMM yyyy')
                                   }
                                   tick={{
                                       fontSize: 8,
                                   }}/>
                            <YAxis/>
                            <Tooltip
                                labelFormatter={(raw: string) =>
                                    DateTime.fromFormat(raw, 'yyyy-LL').toFormat('LLL yyyy')
                                }
                                formatter={(value: number, _, payload) => {
                                    let name = DateTime.fromFormat(payload.payload.name, 'yyyy-LL').toFormat('LLL yyyy')
                                    return [value, name];
                                }}
                            />
                            <Bar dataKey="month" fill="#8884d8"/>
                        </BarChart>
                    </td>
                </tr>
                </tbody>
            </table>
        </>
    )
}
