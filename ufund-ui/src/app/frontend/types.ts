export interface FrontendNeed {
    id: number;
    creatorId: number;
    creatorName: string;
    name: string;
    desc: string; // Ask Harold baby!
    cost: number; // cost in alternative monetary cost
    quantity: number; // number needed for 100.0 completion
    type: string;
    completionPercentage: number; // 0.0 to 100.0 and can overflow
    quantityFulfilled?: number;

    // optional
    icon?: string;

    // UI VALUES
    isExpanded?: boolean
    
    /** Present when need is soft-deleted (archive). */
    is_deleted?: boolean;
    time_deleted?: number;
}

export interface FrontendPledge {
    id: number;
    money: number;
    moneyPledged: number;
    needId: number;
    ownerId: number;
    quantity: number;
    quantityPledged: number;
}