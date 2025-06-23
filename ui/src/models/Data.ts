import type Article from "./Article.ts";
import type {Status} from "./Status.ts";

export default interface Data {
    name: string
    followers: string
    description: string
    articles: Article[]
    status: Status
}
