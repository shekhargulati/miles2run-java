create table activity (id bigint not null, createdAt datetime, version bigint, activityDate datetime not null, distanceCovered double precision not null, duration bigint not null, goalUnit varchar(255) not null, status varchar(1000), goal_id bigint, postedBy_id bigint, share_id bigint, primary key (id));
create table community_run (id bigint not null, createdAt datetime, version bigint, active bit not null, bannerImg varchar(255) not null, description varchar(4000) not null, endDate datetime not null, name varchar(255) not null, slug varchar(255), startDate datetime not null, twitterHandle varchar(255) not null, website varchar(255), primary key (id));
create table community_run_profile (community_run_id bigint not null, profiles_id bigint not null);
create table communityrun_hashtags (communityRun_Id bigint not null, hashtags varchar(255));
create table goal (id bigint not null, createdAt datetime, version bigint, archived bit not null, distance bigint not null, endDate datetime, goalType varchar(255) not null, goalUnit varchar(255), purpose varchar(255) not null, startDate datetime, communityRun_Id bigint, profile_id bigint, primary key (id));
create table profile (id bigint not null, createdAt datetime, version bigint, bio varchar(500), city varchar(255) not null, country varchar(255) not null, email varchar(255) not null, fullname varchar(50) not null, gender varchar(255), profilePic varchar(255), role varchar(255) not null, username varchar(20) not null, primary key (id));
create table share (id bigint not null, createdAt datetime, version bigint, facebook TINYINT(1) not null, googlePlus TINYINT(1) not null, twitter TINYINT(1) not null, primary key (id));
create table social_connection (id bigint not null, createdAt datetime, version bigint, accessSecret varchar(255), accessToken varchar(255) not null, connectionId varchar(255) not null, handle varchar(255), provider varchar(255) not null, profile_id bigint, primary key (id));
alter table community_run add constraint UK_8j9c5402xitdgx41xsanlhhjj  unique (name);
alter table community_run add constraint UK_36mt6qneuyuk79e4kjlv5vd4k  unique (slug);
alter table profile add constraint UK_5em4hwqp4woqsf49dru7fjo80  unique (username);
alter table profile add constraint UK_9d5dpsf2ufa6rjbi3y0elkdcd  unique (email);
alter table activity add constraint FK_gc4upw5javfk56282fwilny20 foreign key (goal_id) references goal (id);
alter table activity add constraint FK_ke6jaidnwub7r90ghbk7a0te9 foreign key (postedBy_id) references profile (id);
alter table activity add constraint FK_t5785e7gww3npdt6rignrb19d foreign key (share_id) references share (id);
alter table community_run_profile add constraint FK_nd6awmoln2u5lmegwweeol00d foreign key (profiles_id) references profile (id);
alter table community_run_profile add constraint FK_ooluk2kx60s3i9igoc9v8klvy foreign key (community_run_id) references community_run (id);
alter table communityrun_hashtags add constraint FK_laxcovjdakegyaoig5ii2i30 foreign key (communityRun_Id) references community_run (id);
alter table goal add constraint FK_i1mflw4g8jr0qf0n7gyfxaxjk foreign key (communityRun_Id) references community_run (id);
alter table goal add constraint FK_fhi96d3255v3f8djjmojj5fqp foreign key (profile_id) references profile (id);
alter table social_connection add constraint FK_rjdwtl9njl9osnelclcn07y8w foreign key (profile_id) references profile (id);
create table id_gen ( sequence_name varchar(255) not null , next_val bigint, primary key ( sequence_name ) );
