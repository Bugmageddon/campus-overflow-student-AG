export type Role = 'STUDENT' | 'TA' | 'TEACHER' | 'ADMIN';

export interface Author { id: number; displayName: string; role: string; verified: boolean }
export interface Page<T> { items: T[]; total: number; page: number; size: number }

export interface Me {
  id: number; username: string; displayName: string; email: string; role: Role;
  verified: boolean; college?: string; className?: string;
  courses: { id: number; code: string; name: string; term: string; teacherName: string }[];
}

export interface QuestionSummary {
  id: number; title: string; excerpt: string; tags: string[]; courseId?: number;
  author: Author; score: number; answerCount: number; viewCount: number; accepted: boolean; createdAt: string;
}

export interface Comment { id: number; author: Author; parentId?: number; body: string; createdAt: string }

export interface Answer {
  id: number; body: string; bodyHtml: string; author: Author; score: number; accepted: boolean;
  endorsedBy?: Author; myVote: number; canEdit: boolean; createdAt: string; comments: Comment[];
}

export interface QuestionDetail {
  id: number; title: string; body: string; bodyHtml: string; tags: string[];
  course?: { id: number; code: string; name: string };
  author: Author; score: number; answerCount: number; viewCount: number; acceptedAnswerId?: number;
  myVote: number; createdAt: string;
  permissions: { canEdit: boolean; canDelete: boolean; canAccept: boolean; canManage: boolean };
  comments: Comment[]; answers: Answer[];
}

export interface SearchResult extends QuestionSummary { bountyOpen: boolean }
export interface LeaderboardEntry { rank: number; userId: number; displayName: string; verified: boolean; score: number }
export interface Notification { id: number; type: string; title: string; link: string; read: boolean; createdAt: string }
export interface ReputationProfile {
  userId: number; displayName: string; reputation: number; availablePoints?: number; frozenPoints?: number;
  badges: { code: string; name: string; description: string; awardedAt: string }[];
}
