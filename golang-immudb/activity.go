package main

type ActivityLog struct {
	ID                string  `json:"id"`
	AssetName         string  `json:"asset_name"`
	AssetType         string  `json:"asset_type"`
	Operation         string  `json:"operation"`
	CreatedAt         string  `json:"created_at"`
	AssetID           string  `json:"asset_id"`
	API               string  `json:"api"`
	Method            string  `json:"method"`
	Size              int64   `json:"size"`
	Role              string  `json:"role"`
	UserID            string  `json:"user_id"`
	OriginServer      string  `json:"origin_server"`
	ShortDescription  string  `json:"short_description"`
	MyactivityEnabled bool    `json:"myactivity_enabled"`
	OrganizationID    *string `json:"org_id"`
	OrganizationName  *string `json:"org_name"`
}

