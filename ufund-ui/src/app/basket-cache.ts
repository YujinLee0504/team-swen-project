import { Pledge } from "./pledge-interface";

export class Cache {
    public static cache: Pledge[] = []
    public static currentUserId: number = 0;
    public static userName: string = ""

    public static cacheId(id: number) {
        Cache.currentUserId = id;
    }

    public static cacheName(name: string) {
        Cache.userName = name;
    }

    public static cacheBasket(pledges: Pledge[]) {
        Cache.cache.length = 0;
        Cache.cache.push(...pledges);
    }
}