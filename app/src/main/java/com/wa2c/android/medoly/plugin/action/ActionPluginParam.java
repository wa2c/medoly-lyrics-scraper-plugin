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
	/** Action ID. */
	public static final String PLUGIN_ACTION_ID = "action_id";
	/** Source Package. */
	public static final String PLUGIN_SRC_PACKAGE= "src_package";
	/** Value Map Key. */
	public static final String PLUGIN_VALUE_KEY  = "value_map";
	/** Event Key. */
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
		TITLE                        ( R.string.media_title                        ),

		/** アーティスト。 */
		ARTIST                       ( R.string.media_artist                       ),
		/** オリジナルアーティスト。 */
		ORIGINAL_ARTIST              ( R.string.media_original_artist              ),
		/** アルバムアーティスト。 */
		ALBUM_ARTIST                 ( R.string.media_album_artist                 ),

		/** アルバム。 */
		ALBUM                        ( R.string.media_album                        ),
		/** オリジナルアルバム。 */
		ORIGINAL_ALBUM               ( R.string.media_original_album               ),

		/** ジャンル。 */
		GENRE                        ( R.string.media_genre                        ),
		/** ムード。 */
		MOOD                         ( R.string.media_mood                         ),
		/** 機会。 */
		OCCASION                     ( R.string.media_occasion                     ),

		/** 年。 */
		YEAR                         ( R.string.media_year                         ),
		/** オリジナル年。 */
		ORIGINAL_YEAR                ( R.string.media_original_year                ),

		/** 作曲者。 */
		COMPOSER                     ( R.string.media_composer                     ),
		/** 編曲者。 */
		ARRANGER                     ( R.string.media_arranger                     ),
		/** 作詞者。 */
		LYRICIST                     ( R.string.media_lyricist                     ),
		/** オリジナル作詞者。 */
		ORIGINAL_LYRICIST            ( R.string.media_original_lyricist            ),
		/** 指揮者  */
		CONDUCTOR                    ( R.string.media_conductor                    ),
		/** プロデューサ。 */
		PRODUCER                     ( R.string.media_producer                     ),
		/** エンジニア。 */
		ENGINEER                     ( R.string.media_engineer                     ),
		/** エンコーダ。 */
		ENCODER                      ( R.string.media_encoder                      ),
		/** ミキサ。 */
		MIXER                        ( R.string.media_mixer                        ),
		/** DJミキサ。 */
		DJMIXER                      ( R.string.media_djmixer                      ),
		/** リミキサ。 */
		REMIXER                      ( R.string.media_remixer                      ),
		/** レーベル。 */
		RECORD_LABEL                 ( R.string.media_record_label                 ),
		/** コメント。 */
		COMMENT                      ( R.string.media_comment                      ),

		/** メディア。 */
		MEDIA                        ( R.string.media_media                        ),
		/** ディスクNo。 */
		DISC                         ( R.string.media_disc                         ),
		/** ディスク合計。 */
		DISC_TOTAL                   ( R.string.media_disc_total                   ),
		/** トラックNo。 */
		TRACK                        ( R.string.media_track                        ),
		/** トラック合計。 */
		TRACK_TOTAL                  ( R.string.media_track_total                  ),

		/** タグ種別。 */
		TAG_TYPE                     ( R.string.media_tag_type                     ),
		/** 文字コード。 */
		CHARACTER_ENCODING           ( R.string.media_character_encoding           ),
		/** 音声フォーマット。 */
		FORMAT                       ( R.string.media_bit_rate                     ),
		/** 音声エンコード種別。 */
		ENCODING_TYPE                ( R.string.media_encoding_type                ),
		/** ビットレート。 */
		BIT_RATE                     ( R.string.media_bit_rate                     ),
		/** サンプリングレート。 */
		SAMPLE_RATE                  ( R.string.media_sample_rate                  ),
		/** 音声チャンネル数。 */
		CHANNELS                     ( R.string.media_channels                     ),
		/** 音声の長さ(ms)。 */
		DURATION                     ( R.string.media_duration                     ),

		/** ループ開始。 */
		LOOP_START                   ( R.string.media_loop                         ),
		/** ループの長さ。  */
		LOOP_LENGTH                  ( R.string.media_loop                         ),
		/** テンポ。 */
		TEMPO                        ( R.string.media_tempo                        ),
		/** BPM。 */
		BPM                          ( R.string.media_bpm                          ),
		/** FBPM。 */
		FBPM                         ( R.string.media_fbpm                         ),

		/** 品質。 */
		QUALITY                      ( R.string.media_quality                      ),
		/** レーティング。  */
		RATING                       ( R.string.media_rating                       ),

		/** 言語。 */
		LANGUAGE                     ( R.string.media_language                     ),
		/** スクリプト。 */
		SCRIPT                       ( R.string.media_script                       ),

		/** タグ。 */
		TAGS                         ( R.string.media_tags                         ),
		/** キー。 */
		KEY                          ( R.string.media_key                          ),

		/** Amazon ID。 */
		AMAZON_ID                    ( R.string.media_amazon_id                    ),
		/** Catalog ID。 */
		CATALOG_NO                   ( R.string.media_catalog_no                   ),
		/** ISRC */
		ISRC                         ( R.string.media_isrc                         ),

		// URI
		URL_OFFICIAL_RELEASE_SITE    ( R.string.media_url_official_release_site    ),
		URL_OFFICIAL_ARTIST_SITE     ( R.string.media_url_official_artist_site     ),
		URL_LYRICS_SITE              ( R.string.media_url_lyrics_site              ),

		// Wikipedia URI
		URL_WIKIPEDIA_RELEASE_SITE   ( R.string.media_url_wikipedia_release_site   ),
		URL_WIKIPEDIA_ARTIST_SITE    ( R.string.media_url_wikipedia_artist_site    ),

		// Discogs URI
		URL_DISCOGS_RELEASE_SITE     ( R.string.media_url_discogs_release_site     ),
		URL_DISCOGS_ARTIST_SITE      ( R.string.media_url_discogs_artist_site      ),

		// Music Brainz Information
		MUSICBRAINZ_RELEASEID        ( R.string.media_musicbrainz_release_id       ),
		MUSICBRAINZ_ARTISTID         ( R.string.media_musicbrainz_artist_id        ),
		MUSICBRAINZ_RELEASEARTISTID  ( R.string.media_musicbrainz_release_artist_id),
		MUSICBRAINZ_RELEASE_GROUP_ID ( R.string.media_musicbrainz_release_group_id ),
		MUSICBRAINZ_DISC_ID          ( R.string.media_musicbrainz_disc_id          ),
		MUSICBRAINZ_TRACK_ID         ( R.string.media_musicbrainz_track_id         ),
		MUSICBRAINZ_WORK_ID          ( R.string.media_musicbrainz_work_id          ),
		MUSICBRAINZ_RELEASE_STATUS   ( R.string.media_musicbrainz_release_status   ),
		MUSICBRAINZ_RELEASE_TYPE     ( R.string.media_musicbrainz_release_type     ),
		MUSICBRAINZ_RELEASE_COUNTRY  ( R.string.media_musicbrainz_release_country  ),

		MUSICIP_ID                   ( R.string.media_musicip_id                   ),


		/** MIMEタイプ。 */
		MIME_TYP                     ( R.string.mime_type                          ),
		/** フォルダパス。 */
		FOLDER_PATH                  ( R.string.folder_path                        ),
		/** ファイル名。 */
		FILE_NAME                    ( R.string.file_name                          ),
		/** データサイズ。 */
		DATA_SIZE                    ( R.string.data_size                          ),
		/** 更新日時。 */
		LAST_MODIFIED                ( R.string.last_modified                      ),

		/**URI*/
		DATA_URI                     ( R.string.data_uri                           );


		/** 名称のID。 */
		private int nameId;
		/** プロパティのキー名。 */
		private String keyName;

		/** 名称を取得。 */
		public String getName(Context context) {
			return context.getString(nameId);
		}
		/** プロパティのキー名を取得。 */
		public String getKeyName() {
			return keyName;
		}

		/** コンストラクタ。 */
		MediaProperty(int nameId) {
			this.nameId = nameId;
			this.keyName = "MEDIA_" + this.name();
		}
	}

	/**
	 * アルバムアートのプロパティ情報。
	 */
	public enum AlbumArtProperty {
		/** リソース種別。 */
		RESOURCE_TYPE ( R.string.album_art_resource_type ),
		/** 画像サイズ。 */
		IMAGE_SIZE    ( R.string.album_art_resolution    ),

		/** MIMEタイプ。 */
		MIME_TYPE     ( R.string.mime_type               ),
		/** フォルダパス。 */
		FOLDER_PATH   ( R.string.folder_path             ),
		/** ファイル名。 */
		FILE_NAME     ( R.string.file_name               ),
		/** データサイズ。 */
		DATA_SIZE     ( R.string.data_size               ),
		/** 更新日時。 */
		LAST_MODIFIED ( R.string.last_modified           ),

		/**URI*/
		DATA_URI      ( R.string.data_uri                );

		/** 名称のID。 */
		private int nameId;
		/** プロパティのキー名。 */
		private String keyName;

		/** 名称を取得。 */
		public String getName(Context context) {
			return context.getString(nameId);
		}
		/** プロパティのキー名を取得。 */
		public String getKeyName() {
			return keyName;
		}

		/** コンストラクタ。 */
		AlbumArtProperty(int nameId) {
			this.nameId = nameId;
			this.keyName = "ALBUM_ART_" + this.name();
		}
	}

	/**
	 * 歌詞のプロパティ情報。
	 */
	public enum LyricsProperty {
		/** 歌詞。 */
		LYRICS             ( R.string.lyrics_lyrics             ),

		/** リソース種別。 */
		RESOURCE_TYPE      ( R.string.lyrics_resource_type      ),
		/** フォーマット種別。 */
		FORMAT_TYPE        ( R.string.lyrics_format_type        ),
		/** 同期種別。 */
		SYNC_TYPE          ( R.string.lyrics_sync_type          ),
		/** オフセット時間(ms)。 */
		OFFSET_TIME        ( R.string.lyrics_offset_time        ),
		/** 文字コード別。 */
		CHARACTER_ENCODING ( R.string.lyrics_character_encoding ),

		/** MIME Type */
		MIME_TYPE          ( R.string.mime_type                 ),
		/** フォルダパス。 */
		FOLDER_PATH        ( R.string.folder_path               ),
		/** ファイル名。 */
		FILE_NAME          ( R.string.file_name                 ),
		/** データサイズ。 */
		DATA_SIZE          ( R.string.data_size                 ),
		/** 更新日時。 */
		LAST_MODIFIED      ( R.string.last_modified             ),

		/**URI*/
		DATA_URI           ( R.string.data_uri                  );

		/** 名称のID。 */
		private int nameId;
		/** プロパティのキー名。 */
		private String keyName;

		/** 名称を取得。 */
		public String getName(Context context) {
			return context.getString(nameId);
		}
		/** プロパティのキー名を取得。 */
		public String getKeyName() {
			return keyName;
		}

		/** コンストラクタ。 */
		LyricsProperty(int nameId) {
			this.nameId = nameId;
			this.keyName = "LYRICS_" + this.name();
		}
	}

	/**
	 * 再生キューのプロパティ情報。
	 */
	public enum QueueProperty {
		CURRENT_POSITION ( R.string.queue_current_position ),
		/** 現在の再生曲No。 */
		CURRENT_NO       ( R.string.queue_current_no       ),
		/** 合計再生曲数。 */
		TOTAL_COUNT      ( R.string.queue_total_count      ),
		/** 再生済曲数。 */
		PLAYED_COUNT     ( R.string.queue_played_count     ),
		/** 合計再生時間。 */
		TOTAL_TIME       ( R.string.queue_total_time       ),
		/** 再生済時間。 */
		PLAYED_TIME      ( R.string.queue_played_time      ),
		/** ループ回数。 */
		LOOP_COUNT       ( R.string.queue_loop_count       );

		/** 名称のID。 */
		private int nameId;
		/** プロパティのキー名。 */
		private String keyName;

		/** 名称を取得。 */
		public String getName(Context context) {
			return context.getString(nameId);
		}
		/** プロパティのキー名を取得。 */
		public String getKeyName() {
			return keyName;
		}
		
		/** コンストラクタ。 */
		QueueProperty(int nameId) {
			this.nameId = nameId;
			this.keyName = "QUEUE_" + this.name();
		}
	}

}
