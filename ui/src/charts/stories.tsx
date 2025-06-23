import type Props from "../models/props.ts";
import publishedDate from "../Utils.ts";

export default function Stories(props: Props) {
    const images = props.articles.map(article => ({
        url: article.imageUrl,
        alt: article.imageAlt,
        link: article.link,
        year: publishedDate(article.published).year,
        title: article.title,
    }));

    const groupedByYear = images.reduce((acc, item) => {
        if (!acc[item.year]) {
            acc[item.year] = [];
        }
        acc[item.year].push(item);
        return acc;
    }, {} as Record<number, any>);

    const title = (image: any) => image.title

    return (
        <>
            <h2 style={{margin: '2rem'}}>Stories</h2>

            {Object.entries(groupedByYear)
                .sort(([a], [b]) => Number(b) - Number(a))
                .map(([year, items]) => (

                    <div key={year} style={{marginBottom: '1.5rem'}}>
                        <details>
                            <summary>{year}</summary>
                            <div
                                style={{
                                    display: "grid",
                                    gridTemplateColumns: `repeat(${Math.ceil(Math.sqrt(items.length))}, 1fr)`,
                                    gap: "1px",
                                }}
                            >
                                {items.map((image: any, i: number) => (
                                    <a href={image.link} key={i} target="_blank">
                                        <img
                                            key={i}
                                            src={image.url}
                                            alt={image.alt}
                                            title={title(image)}
                                            height={100}
                                            style={{width: "100%", aspectRatio: "1 / 1", objectFit: "cover"}}
                                        />
                                    </a>
                                ))}
                            </div>
                        </details>
                    </div>
                ))}
        </>
    )
}
