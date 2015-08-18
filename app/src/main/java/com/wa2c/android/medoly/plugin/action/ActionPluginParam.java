package com.wa2c.android.medoly.plugin.action;

import android.content.Context;

import com.wa2c.android.medoly.plugin.action.lyricsscraper.R;


/**
 * プラグイン共通パラメータ。
 */
public class ActionPluginParam {

	/** Medoly package.. */
	public static String MEDOLY_PACKAGE = "com.wa2c.android.medoly";

	/** Action. */
	public static final String PLUGIN_ACTION = "com.wa2c.android.medoly.plugin.action.ACTION_MEDIA";
	/** アクションID。 */
	public static final String PLUGIN_ACTION_ID = "action_id";
	/** 値マップのキー。 */
	public static final String PLUGIN_VALUE_KEY  = "value_map";
	/** イベントキー。 */
	public static final String PLUGIN_EVENT_KEY = "is_event";



	/** プラグイン種別カテゴリ */
	public enum PluginTypeCategory {
		/** メッセージ通知。 */
		TYPE_POST_MESSAGE,
		/** アルバムアート要求。 */
		TYPE_GET_ALBUM_ART,
		/** 歌詞要求。 */
		TYPE_GET_LYRICS,

		/** アルバムアート取得。 */
		TYPE_PUT_ALBUM_ART,
		/** 歌詞取得。 */
		TYPE_PUT_LYRICS;

		/** コンストラクタ。 */
		PluginTypeCategory() {
			categoryValue = "com.wa2c.android.medoly.plugin.category." + this.name();
		}

		/** カテゴリの値。 */
		private String categoryValue;
		/** カテゴリの値を取得。 */
		public String getCategoryValue() {
			return categoryValue;
		}
	}

	/** プラグイン操作カテゴリ。 */
	public enum PluginOperationCategory {
		/** 実行 */
		OPERATION_EXECUTE,
		/** メディア開始。 */
		OPERATION_MEDIA_OPEN,
		/** 再生開始。 */
		OPERATION_PLAY_START,
		/** 再生中。 */
		OPERATION_PLAY_NOW,
		/** 再生停止。 */
		OPERATION_PLAY_STOP,
		/** 再生完了。 */
		OPERATION_PLAY_COMPLETE,
		/** メディア終了。 */
		OPERATION_MEDIA_CLOSE;

		/** コンストラクタ。 */
		PluginOperationCategory() {
			categoryValue = "com.wa2c.android.medoly.plugin.category." + this.name();
		}

		/** カテゴリの値。 */
		private String categoryValue;
		/** カテゴリの値を取得。 */
		public String getCategoryValue() {
			return categoryValue;
		}
	}



	//
	// Property
	//

	/**
	 * メディアのプロパティ情報。
	 */
	public enum MediaProperty {
		/** タイトル。 */
		TITLE                        ( R.string.media_title                        , true  ),

		/** アーティスト。 */
		ARTIST                       ( R.string.media_artist                       , true  ),
		/** オリジナルアーティスト。 */
		ORIGINAL_ARTIST              ( R.string.media_original_artist              , true  ),
		/** アルバムアーティスト。 */
		ALBUM_ARTIST                 ( R.string.media_album_artist                 , true  ),

		/** アルバム。 */
		ALBUM                        ( R.string.media_album                        , true  ),
		/** オリジナルアルバム。 */
		ORIGINAL_ALBUM               ( R.string.media_original_album               , true  ),

		/** ジャンル。 */
		GENRE                        ( R.string.media_genre                        , true  ),
		/** ムード。 */
		MOOD                         ( R.string.media_mood                         , true  ),
		/** 機会。 */
		OCCASION                     ( R.string.media_occasion                     , true  ),

		/** 年。 */
		YEAR                         ( R.string.media_year                         , false ),
		/** オリジナル年。 */
		ORIGINAL_YEAR                ( R.string.media_original_year                , false ),

		/** 作曲者。 */
		COMPOSER                     ( R.string.media_composer                     , true  ),
		/** 編曲者。 */
		ARRANGER                     ( R.string.media_arranger                     , true  ),
		/** 作詞者。 */
		LYRICIST                     ( R.string.media_lyricist                     , true  ),
		/** オリジナル作詞者。 */
		ORIGINAL_LYRICIST            ( R.string.media_original_lyricist            , true  ),
		/** 指揮者  */
		CONDUCTOR                    ( R.string.media_conductor                    , true  ),
		/** プロデューサ。 */
		PRODUCER                     ( R.string.media_producer                     , true  ),
		/** エンジニア。 */
		ENGINEER                     ( R.string.media_engineer                     , true  ),
		/** エンコーダ。 */
		ENCODER                      ( R.string.media_encoder                      , true  ),
		/** ミキサ。 */
		MIXER                        ( R.string.media_mixer                        , true  ),
		/** DJミキサ。 */
		DJMIXER                      ( R.string.media_djmixer                      , true  ),
		/** リミキサ。 */
		REMIXER                      ( R.string.media_remixer                      , true  ),
		/** レーベル。 */
		RECORD_LABEL                 ( R.string.media_record_label                 , true  ),
		/** コメント。 */
		COMMENT                      ( R.string.media_comment                      , true  ),

		/** メディア。 */
		MEDIA                        ( R.string.media_media                        , false ),
		/** ディスクNo。 */
		DISC                         ( R.string.media_disc                         , false ),
		/** ディスク合計。 */
		DISC_TOTAL                   ( R.string.media_disc_total                   , false ),
		/** トラックNo。 */
		TRACK                        ( R.string.media_track                        , false ),
		/** トラック合計。 */
		TRACK_TOTAL                  ( R.string.media_track_total                  , false ),

		/** タグ種別。 */
		TAG_TYPE                     ( R.string.media_tag_type                     , false ),
		/** 文字コード。 */
		CHARACTER_ENCODING           ( R.string.media_character_encoding           , false ),
		/** 音声フォーマット。 */
		FORMAT                       ( R.string.media_bit_rate                     , false ),
		/** 音声エンコード種別。 */
		ENCODING_TYPE                ( R.string.media_encoding_type                , false ),
		/** ビットレート。 */
		BIT_RATE                     ( R.string.media_bit_rate                     , false ),
		/** サンプリングレート。 */
		SAMPLE_RATE                  ( R.string.media_sample_rate                  , false ),
		/** 音声チャンネル数。 */
		CHANNELS                     ( R.string.media_channels                     , false ),
		/** 音声の長さ(ms)。 */
		DURATION                     ( R.string.media_duration                     , false ),

		/** ループ開始。 */
		LOOP_START                   ( R.string.media_loop                         , false ),
		/** ループの長さ。  */
		LOOP_LENGTH                  ( R.string.media_loop                         , false ),
		/** テンポ。 */
		TEMPO                        ( R.string.media_tempo                        , false ),
		/** BPM。 */
		BPM                          ( R.string.media_bpm                          , false ),
		/** FBPM。 */
		FBPM                         ( R.string.media_fbpm                         , false ),

		/** 品質。 */
		QUALITY                      ( R.string.media_quality                      , false ),
		/** レーティング。  */
		RATING                       ( R.string.media_rating                       , false ),

		/** 言語。 */
		LANGUAGE                     ( R.string.media_language                     , false ),
		/** スクリプト。 */
		SCRIPT                       ( R.string.media_script                       , false ),

		/** タグ。 */
		TAGS                         ( R.string.media_tags                         , false ),
		/** キー。 */
		KEY                          ( R.string.media_key                          , false ),

		/** Amazon ID。 */
		AMAZON_ID                    ( R.string.media_amazon_id                    , false ),
		/** Catalog ID。 */
		CATALOG_NO                   ( R.string.media_catalog_no                   , false ),
		/** ISRC */
		ISRC                         ( R.string.media_isrc                         , false ),

		// URI
		URL_OFFICIAL_RELEASE_SITE    ( R.string.media_url_official_release_site    , false ),
		URL_OFFICIAL_ARTIST_SITE     ( R.string.media_url_official_artist_site     , false ),
		URL_LYRICS_SITE              ( R.string.media_url_lyrics_site              , false ),

		// Wikipedia URI
		URL_WIKIPEDIA_RELEASE_SITE   ( R.string.media_url_wikipedia_release_site   , false),
		URL_WIKIPEDIA_ARTIST_SITE    ( R.string.media_url_wikipedia_artist_site    , false),

		// Discogs URI
		URL_DISCOGS_RELEASE_SITE     ( R.string.media_url_discogs_release_site     , false ),
		URL_DISCOGS_ARTIST_SITE      ( R.string.media_url_discogs_artist_site      , false ),

		// Music Brainz Information
		MUSICBRAINZ_RELEASEID        ( R.string.media_musicbrainz_release_id       , false ),
		MUSICBRAINZ_ARTISTID         ( R.string.media_musicbrainz_artist_id        , false ),
		MUSICBRAINZ_RELEASEARTISTID  ( R.string.media_musicbrainz_release_artist_id, false ),
		MUSICBRAINZ_RELEASE_GROUP_ID ( R.string.media_musicbrainz_release_group_id , false ),
		MUSICBRAINZ_DISC_ID          ( R.string.media_musicbrainz_disc_id          , false ),
		MUSICBRAINZ_TRACK_ID         ( R.string.media_musicbrainz_track_id         , false ),
		MUSICBRAINZ_WORK_ID          ( R.string.media_musicbrainz_work_id          , false ),
		MUSICBRAINZ_RELEASE_STATUS   ( R.string.media_musicbrainz_release_status   , false ),
		MUSICBRAINZ_RELEASE_TYPE     ( R.string.media_musicbrainz_release_type     , false ),
		MUSICBRAINZ_RELEASE_COUNTRY  ( R.string.media_musicbrainz_release_country  , false ),

		MUSICIP_ID                   ( R.string.media_musicip_id                   , false),


		/** MIMEタイプ。 */
		MIME_TYP                     ( R.string.mime_type                          , false ),
		/** フォルダパス。 */
		FOLDER_PATH                  ( R.string.folder_path                        , true  ),
		/** ファイル名。 */
		FILE_NAME                    ( R.string.file_name                          , true  ),
		/** データサイズ。 */
		DATA_SIZE                    ( R.string.data_size                          , false ),
		/** 更新日時。 */
		LAST_MODIFIED                ( R.string.last_modified                      , true  );


		/** 名称のID。 */
		private int nameId;
		/** プロパティのキー名。 */
		private String keyName;
		/** 省略可否。 */
		private boolean omissible;

		/** 名称を取得。 */
		public String getName(Context context) {
			return context.getString(nameId);
		}
		/** プロパティのキー名を取得。 */
		public String getKeyName() {
			return keyName;
		}
		/** 省略可否を取得。 */
		public boolean getOmissible() {
			return omissible;
		}

		/** コンストラクタ。 */
		MediaProperty(int nameId, boolean omissible) {
			this.nameId = nameId;
			this.keyName = "MEDIA_" + this.name();
			this.omissible = omissible;
		}
	}

	/**
	 * アルバムアートのプロパティ情報。
	 */
	public enum AlbumArtProperty {
		/** リソース種別。 */
		RESOURCE_TYPE ( R.string.album_art_resource_type , true  ),
		/** 画像サイズ。 */
		IMAGE_SIZE    ( R.string.album_art_resolution    , false ),

		/** MIMEタイプ。 */
		MIME_TYPE     ( R.string.mime_type               , false ),
		/** フォルダパス。 */
		FOLDER_PATH   ( R.string.folder_path             , true  ),
		/** ファイル名。 */
		FILE_NAME     ( R.string.file_name               , true  ),
		/** データサイズ。 */
		DATA_SIZE     ( R.string.data_size               , false ),
		/** 更新日時。 */
		LAST_MODIFIED ( R.string.last_modified           , true  );

		/** 名称のID。 */
		private int nameId;
		/** プロパティのキー名。 */
		private String keyName;
		/** 省略可否。 */
		private boolean omissible;

		/** 名称を取得。 */
		public String getName(Context context) {
			return context.getString(nameId);
		}
		/** プロパティのキー名を取得。 */
		public String getKeyName() {
			return keyName;
		}
		/** 省略可否を取得。 */
		public boolean getOmissible() {
			return omissible;
		}


		/** コンストラクタ。 */
		AlbumArtProperty(int nameId, boolean omissible) {
			this.nameId = nameId;
			this.keyName = "ALBUM_ART_" + this.name();
			this.omissible = omissible;
		}
	}

	/**
	 * 歌詞のプロパティ情報。
	 */
	public enum LyricsProperty {
		/** 歌詞。 */
		LYRICS             ( R.string.lyrics_lyrics             , true  ),

		/** リソース種別。 */
		RESOURCE_TYPE      ( R.string.lyrics_resource_type      , true  ),
		/** フォーマット種別。 */
		FORMAT_TYPE        ( R.string.lyrics_format_type        , true  ),
		/** 同期種別。 */
		SYNC_TYPE          ( R.string.lyrics_sync_type          , true  ),
		/** オフセット時間(ms)。 */
		OFFSET_TIME        ( R.string.lyrics_offset_time        , false ),
		/** 文字コード別。 */
		CHARACTER_ENCODING ( R.string.lyrics_character_encoding , false ),

		/** MIME Type */
		MIME_TYPE          ( R.string.mime_type                 , false ),
		/** フォルダパス。 */
		FOLDER_PATH        ( R.string.folder_path               , true  ),
		/** ファイル名。 */
		FILE_NAME          ( R.string.file_name                 , true  ),
		/** データサイズ。 */
		DATA_SIZE          ( R.string.data_size                 , false ),
		/** 更新日時。 */
		LAST_MODIFIED      ( R.string.last_modified             , true  );

		/** 名称のID。 */
		private int nameId;
		/** プロパティのキー名。 */
		private String keyName;
		/** 省略可否。 */
		private boolean omissible;

		/** 名称を取得。 */
		public String getName(Context context) {
			return context.getString(nameId);
		}
		/** プロパティのキー名を取得。 */
		public String getKeyName() {
			return keyName;
		}
		/** 省略可否を取得。 */
		public boolean getOmissible() {
			return omissible;
		}

		/** コンストラクタ。 */
		LyricsProperty(int nameId, boolean omissible) {
			this.nameId = nameId;
			this.keyName = "LYRICS_" + this.name();
			this.omissible = omissible;
		}
	}

	/**
	 * 再生キューのプロパティ情報。
	 */
	public enum QueueProperty {
		CURRENT_POSITION ( R.string.queue_current_position, false ),
		/** 現在の再生曲No。 */
		CURRENT_NO       ( R.string.queue_current_no      , false ),
		/** 合計再生曲数。 */
		TOTAL_COUNT      ( R.string.queue_total_count     , false ),
		/** 再生済曲数。 */
		PLAYED_COUNT     ( R.string.queue_played_count    , false ),
		/** 合計再生時間。 */
		TOTAL_TIME       ( R.string.queue_total_time      , false ),
		/** 再生済時間。 */
		PLAYED_TIME      ( R.string.queue_played_time     , false ),
		/** ループ回数。 */
		LOOP_COUNT       ( R.string.queue_loop_count      , false );

		/** 名称のID。 */
		private int nameId;
		/** プロパティのキー名。 */
		private String keyName;
		/** 省略可否。 */
		private boolean omissible;

		/** 名称を取得。 */
		public String getName(Context context) {
			return context.getString(nameId);
		}
		/** プロパティのキー名を取得。 */
		public String getKeyName() {
			return keyName;
		}
		/** 省略可否を取得。 */
		public boolean getOmissible() {
			return omissible;
		}
		/** コンストラクタ。 */
		QueueProperty(int nameId, boolean omissible) {
			this.nameId = nameId;
			this.keyName = "QUEUE_" + this.name();
			this.omissible = omissible;
		}
	}

}
