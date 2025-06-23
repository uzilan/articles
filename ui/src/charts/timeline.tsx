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

    return (
        <>
            <h2 style={{margin: '2rem'}}>Timeline</h2>

            <table style={{width: '100%'}}>
                <tbody>
                <tr style={{width: '100%', textAlign: 'center'}}>
                    <td>
                        <h3>Stories per year (average: {avg(Object.values(years))})</h3>
                    </td>
                </tr>
                <tr style={{width: '100%', textAlign: 'center'}}>
                    <td style={{width: '100%', textAlign: 'center'}}>
                        <BarChart width={1000} height={200} data={Object.entries(years).map(d => ({
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
                </tr>
                <tr>
                    <td>
                        <h3>Stories per month and year (average: {avg(Object.values(groupedByMonths))})</h3>
                    </td>
                </tr>
                <tr style={{width: '100%', textAlign: 'center'}}>
                    <td style={{width: '100%', textAlign: 'center'}}>
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
