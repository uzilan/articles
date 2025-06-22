import {DateTime} from "luxon";

export default function publishedDate(published: string): DateTime {
    return DateTime.fromSQL(published);
}
