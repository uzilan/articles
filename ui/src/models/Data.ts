import type Article from "./Article.ts";

export default interface Data {
    name: string
    followers: string
    description: string
    articles: Article[]
}
