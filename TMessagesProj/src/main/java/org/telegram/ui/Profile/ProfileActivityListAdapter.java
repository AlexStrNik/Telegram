package org.telegram.ui.Profile;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.Stars.StarsIntroActivity.formatStarsAmountShort;
import static org.telegram.ui.bots.AffiliateProgramFragment.percents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BirthdayController;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_bots;
import org.telegram.tgnet.tl.TL_fragment;
import org.telegram.tgnet.tl.TL_stars;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionIntroActivity;
import org.telegram.ui.Business.ProfileHoursCell;
import org.telegram.ui.Business.ProfileLocationCell;
import org.telegram.ui.Cells.AboutLinkCell;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ProfileChannelCell;
import org.telegram.ui.Cells.SettingsSuggestionCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.ChannelMonetizationLayout;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedEmojiSpan;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.IdenticonDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Premium.PremiumGradient;
import org.telegram.ui.Components.Premium.ProfilePremiumCell;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.FragmentUsernameBottomSheet;
import org.telegram.ui.Stars.BotStarsController;
import org.telegram.ui.Stars.StarsController;
import org.telegram.ui.Stars.StarsIntroActivity;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;
import org.telegram.ui.TwoStepVerificationSetupActivity;
import org.telegram.ui.UserInfoActivity;
import org.telegram.ui.bots.AffiliateProgramFragment;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

class ProfileActivityListAdapter extends RecyclerListView.SelectionAdapter {
    private final static int VIEW_TYPE_HEADER = 1;
    private final static int VIEW_TYPE_TEXT_DETAIL = 2;
    private final static int VIEW_TYPE_ABOUT_LINK = 3;
    private final static int VIEW_TYPE_TEXT = 4;
    private final static int VIEW_TYPE_DIVIDER = 5;
    private final static int VIEW_TYPE_NOTIFICATIONS_CHECK = 6;
    private final static int VIEW_TYPE_SHADOW = 7;
    private final static int VIEW_TYPE_USER = 8;
    private final static int VIEW_TYPE_EMPTY = 11;
    private final static int VIEW_TYPE_BOTTOM_PADDING = 12;
    final static int VIEW_TYPE_SHARED_MEDIA = 13;
    private final static int VIEW_TYPE_VERSION = 14;
    private final static int VIEW_TYPE_SUGGESTION = 15;
    private final static int VIEW_TYPE_ADDTOGROUP_INFO = 17;
    private final static int VIEW_TYPE_PREMIUM_TEXT_CELL = 18;
    private final static int VIEW_TYPE_TEXT_DETAIL_MULTILINE = 19;
    private final static int VIEW_TYPE_NOTIFICATIONS_CHECK_SIMPLE = 20;
    private final static int VIEW_TYPE_LOCATION = 21;
    private final static int VIEW_TYPE_HOURS = 22;
    private final static int VIEW_TYPE_CHANNEL = 23;
    private final static int VIEW_TYPE_STARS_TEXT_CELL = 24;
    private final static int VIEW_TYPE_BOT_APP = 25;
    private final static int VIEW_TYPE_SHADOW_TEXT = 26;
    private final static int VIEW_TYPE_COLORFUL_TEXT = 27;

    private final ProfileActivity profileActivity;
    private Context mContext;

    public ProfileActivityListAdapter(ProfileActivity profileActivity, Context context) {
        this.profileActivity = profileActivity;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_HEADER: {
                view = new HeaderCell(mContext, 23, profileActivity.resourcesProvider);
                view.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case VIEW_TYPE_TEXT_DETAIL_MULTILINE:
            case VIEW_TYPE_TEXT_DETAIL:
                final TextDetailCell textDetailCell = new TextDetailCell(mContext, profileActivity.resourcesProvider, viewType == VIEW_TYPE_TEXT_DETAIL_MULTILINE) {
                    @Override
                    protected int processColor(int color) {
                        return profileActivity.dontApplyPeerColor(color, false);
                    }
                };
                textDetailCell.setContentDescriptionValueFirst(true);
                view = textDetailCell;
                view.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            case VIEW_TYPE_ABOUT_LINK: {
                view = profileActivity.aboutLinkCell = new AboutLinkCell(mContext, profileActivity, profileActivity.resourcesProvider) {
                    @Override
                    protected void didPressUrl(String url, Browser.Progress progress) {
                        profileActivity.openUrl(url, progress);
                    }

                    @Override
                    protected void didResizeEnd() {
                        profileActivity.layoutManager.mIgnoreTopPadding = false;
                    }

                    @Override
                    protected void didResizeStart() {
                        profileActivity.layoutManager.mIgnoreTopPadding = true;
                    }

                    @Override
                    protected int processColor(int color) {
                        return profileActivity.dontApplyPeerColor(color, false);
                    }
                };
                view.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case VIEW_TYPE_TEXT: {
                view = new TextCell(mContext, profileActivity.resourcesProvider) {
                    @Override
                    protected int processColor(int color) {
                        return profileActivity.dontApplyPeerColor(color, false);
                    }
                };
                view.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case VIEW_TYPE_DIVIDER: {
                view = new DividerCell(mContext, profileActivity.resourcesProvider);
                view.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                view.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(4), 0, 0);
                break;
            }
            case VIEW_TYPE_NOTIFICATIONS_CHECK: {
                view = new NotificationsCheckCell(mContext, 23, 70, false, profileActivity.resourcesProvider) {
                    @Override
                    protected int processColor(int color) {
                        return profileActivity.dontApplyPeerColor(color, false);
                    }
                };
                view.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case VIEW_TYPE_NOTIFICATIONS_CHECK_SIMPLE: {
                view = new TextCheckCell(mContext, profileActivity.resourcesProvider);
                view.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case VIEW_TYPE_SHADOW: {
                view = new ShadowSectionCell(mContext, profileActivity.resourcesProvider);
                break;
            }
            case VIEW_TYPE_SHADOW_TEXT: {
                view = new TextInfoPrivacyCell(mContext, profileActivity.resourcesProvider);
                break;
            }
            case VIEW_TYPE_COLORFUL_TEXT: {
                view = new AffiliateProgramFragment.ColorfulTextCell(mContext, profileActivity.resourcesProvider);
                view.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case VIEW_TYPE_USER: {
                view = new UserCell(mContext, profileActivity.addMemberRow == -1 ? 9 : 6, 0, true, profileActivity.resourcesProvider);
                view.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case VIEW_TYPE_EMPTY: {
                view = new View(mContext) {
                    @Override
                    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(32), MeasureSpec.EXACTLY));
                    }
                };
                break;
            }
            case VIEW_TYPE_BOTTOM_PADDING: {
                view = new View(mContext) {

                    private int lastPaddingHeight = 0;
                    private int lastListViewHeight = 0;

                    @Override
                    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                        if (lastListViewHeight != profileActivity.listView.getMeasuredHeight()) {
                            lastPaddingHeight = 0;
                        }
                        lastListViewHeight = profileActivity.listView.getMeasuredHeight();
                        int n = profileActivity.listView.getChildCount();
                        if (n == profileActivity.listAdapter.getItemCount()) {
                            int totalHeight = 0;
                            for (int i = 0; i < n; i++) {
                                View view = profileActivity.listView.getChildAt(i);
                                int p = profileActivity.listView.getChildAdapterPosition(view);
                                if (p >= 0 && p != profileActivity.bottomPaddingRow) {
                                    totalHeight += profileActivity.listView.getChildAt(i).getMeasuredHeight();
                                }
                            }
                            int paddingHeight = (profileActivity.fragmentView == null ? 0 : profileActivity.fragmentView.getMeasuredHeight()) - ActionBar.getCurrentActionBarHeight() - AndroidUtilities.statusBarHeight - totalHeight;
                            if (paddingHeight > AndroidUtilities.dp(88)) {
                                paddingHeight = 0;
                            }
                            if (paddingHeight <= 0) {
                                paddingHeight = 0;
                            }
                            setMeasuredDimension(profileActivity.listView.getMeasuredWidth(), lastPaddingHeight = paddingHeight);
                        } else {
                            setMeasuredDimension(profileActivity.listView.getMeasuredWidth(), lastPaddingHeight);
                        }
                    }
                };
                view.setBackground(new ColorDrawable(Color.TRANSPARENT));
                break;
            }
            case VIEW_TYPE_SHARED_MEDIA: {
                if (profileActivity.sharedMediaLayout.getParent() != null) {
                    ((ViewGroup) profileActivity.sharedMediaLayout.getParent()).removeView(profileActivity.sharedMediaLayout);
                }
                view = profileActivity.sharedMediaLayout;
                break;
            }
            case VIEW_TYPE_ADDTOGROUP_INFO: {
                view = new TextInfoPrivacyCell(mContext, profileActivity.resourcesProvider);
                view.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            }
            case VIEW_TYPE_LOCATION:
                view = new ProfileLocationCell(mContext, profileActivity.resourcesProvider);
                view.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            case VIEW_TYPE_HOURS:
                view = new ProfileHoursCell(mContext, profileActivity.resourcesProvider) {
                    @Override
                    protected int processColor(int color) {
                        return profileActivity.dontApplyPeerColor(color, false);
                    }
                };
                view.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            case VIEW_TYPE_VERSION:
            default: {
                TextInfoPrivacyCell cell = new TextInfoPrivacyCell(mContext, 10, profileActivity.resourcesProvider);
                cell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
                cell.getTextView().setTextColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhiteGrayText3));
                cell.getTextView().setMovementMethod(null);
                try {
                    PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                    int code = pInfo.versionCode / 10;
                    String abi = "";
                    switch (pInfo.versionCode % 10) {
                        case 1:
                        case 2:
                            abi = "store bundled " + Build.CPU_ABI + " " + Build.CPU_ABI2;
                            break;
                        default:
                        case 9:
                            if (ApplicationLoader.isStandaloneBuild()) {
                                abi = "direct " + Build.CPU_ABI + " " + Build.CPU_ABI2;
                            } else {
                                abi = "universal " + Build.CPU_ABI + " " + Build.CPU_ABI2;
                            }
                            break;
                    }
                    cell.setText(formatString("TelegramVersion", R.string.TelegramVersion, String.format(Locale.US, "v%s (%d) %s", pInfo.versionName, code, abi)));
                } catch (Exception e) {
                    FileLog.e(e);
                }
                cell.getTextView().setPadding(0, AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14));
                view = cell;
                view.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, profileActivity.getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                break;
            }
            case VIEW_TYPE_SUGGESTION: {
                view = new SettingsSuggestionCell(mContext, profileActivity.resourcesProvider) {
                    @Override
                    protected void onYesClick(int type) {
                        AndroidUtilities.runOnUIThread(() -> {
                            profileActivity.getNotificationCenter().removeObserver(profileActivity, NotificationCenter.newSuggestionsAvailable);
                            if (type == SettingsSuggestionCell.TYPE_GRACE) {
                                profileActivity.getMessagesController().removeSuggestion(0, "PREMIUM_GRACE");
                                Browser.openUrl(getContext(), profileActivity.getMessagesController().premiumManageSubscriptionUrl);
                            } else {
                                profileActivity.getMessagesController().removeSuggestion(0, type == SettingsSuggestionCell.TYPE_PHONE ? "VALIDATE_PHONE_NUMBER" : "VALIDATE_PASSWORD");
                            }
                            profileActivity.getNotificationCenter().addObserver(profileActivity, NotificationCenter.newSuggestionsAvailable);
                            profileActivity.updateListAnimated(false);
                        });
                    }

                    @Override
                    protected void onNoClick(int type) {
                        if (type == SettingsSuggestionCell.TYPE_PHONE) {
                            profileActivity.presentFragment(new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_CHANGE_PHONE_NUMBER));
                        } else {
                            profileActivity.presentFragment(new TwoStepVerificationSetupActivity(TwoStepVerificationSetupActivity.TYPE_VERIFY, null));
                        }
                    }
                };
                break;
            }
            case VIEW_TYPE_PREMIUM_TEXT_CELL:
            case VIEW_TYPE_STARS_TEXT_CELL:
                view = new ProfilePremiumCell(mContext, viewType == VIEW_TYPE_PREMIUM_TEXT_CELL ? 0 : 1, profileActivity.resourcesProvider);
                view.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            case VIEW_TYPE_CHANNEL:
                view = new ProfileChannelCell(profileActivity) {
                    @Override
                    public int processColor(int color) {
                        return profileActivity.dontApplyPeerColor(color, false);
                    }
                };
                view.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
            case VIEW_TYPE_BOT_APP:
                FrameLayout frameLayout = new FrameLayout(mContext);
                ButtonWithCounterView button = new ButtonWithCounterView(mContext, profileActivity.resourcesProvider);
                button.setText(LocaleController.getString(R.string.ProfileBotOpenApp), false);
                button.setOnClickListener(v -> {
                    TLRPC.User bot = profileActivity.getMessagesController().getUser(profileActivity.userId);
                    profileActivity.getMessagesController().openApp(profileActivity, bot, null, profileActivity.getClassGuid(), null);
                });
                frameLayout.addView(button, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.FILL, 18, 14, 18, 14));
                view = frameLayout;
                view.setBackgroundColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhite));
                break;
        }
        if (viewType != VIEW_TYPE_SHARED_MEDIA) {
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        }
        return new RecyclerListView.Holder(view);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (holder.itemView == profileActivity.sharedMediaLayout) {
            profileActivity.sharedMediaLayoutAttached = true;
        }
        if (holder.itemView instanceof TextDetailCell) {
            ((TextDetailCell) holder.itemView).textView.setLoading(profileActivity.loadingSpan);
            ((TextDetailCell) holder.itemView).valueTextView.setLoading(profileActivity.loadingSpan);
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if (holder.itemView == profileActivity.sharedMediaLayout) {
            profileActivity.sharedMediaLayoutAttached = false;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HEADER:
                HeaderCell headerCell = (HeaderCell) holder.itemView;
                if (position == profileActivity.infoHeaderRow) {
                    if (ChatObject.isChannel(profileActivity.currentChat) && !profileActivity.currentChat.megagroup && profileActivity.channelInfoRow != -1) {
                        headerCell.setText(LocaleController.getString(R.string.ReportChatDescription));
                    } else {
                        headerCell.setText(LocaleController.getString(R.string.Info));
                    }
                } else if (position == profileActivity.membersHeaderRow) {
                    headerCell.setText(LocaleController.getString(R.string.ChannelMembers));
                } else if (position == profileActivity.settingsSectionRow2) {
                    headerCell.setText(LocaleController.getString(R.string.SETTINGS));
                } else if (position == profileActivity.numberSectionRow) {
                    headerCell.setText(LocaleController.getString(R.string.Account));
                } else if (position == profileActivity.helpHeaderRow) {
                    headerCell.setText(LocaleController.getString(R.string.SettingsHelp));
                } else if (position == profileActivity.debugHeaderRow) {
                    headerCell.setText(LocaleController.getString(R.string.SettingsDebug));
                } else if (position == profileActivity.botPermissionsHeader) {
                    headerCell.setText(LocaleController.getString(R.string.BotProfilePermissions));
                }
                headerCell.setTextColor(profileActivity.dontApplyPeerColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader), false));
                break;
            case VIEW_TYPE_TEXT_DETAIL_MULTILINE:
            case VIEW_TYPE_TEXT_DETAIL:
                TextDetailCell detailCell = (TextDetailCell) holder.itemView;
                boolean containsQr = false;
                boolean containsGift = false;
                if (position == profileActivity.birthdayRow) {
                    TLRPC.UserFull userFull = profileActivity.getMessagesController().getUserFull(profileActivity.userId);
                    if (userFull != null && userFull.birthday != null) {
                        final boolean today = BirthdayController.isToday(userFull);
                        final boolean withYear = (userFull.birthday.flags & 1) != 0;
                        final int age = withYear ? Period.between(LocalDate.of(userFull.birthday.year, userFull.birthday.month, userFull.birthday.day), LocalDate.now()).getYears() : -1;

                        String text = UserInfoActivity.birthdayString(userFull.birthday);

                        if (withYear) {
                            text = LocaleController.formatPluralString(today ? "ProfileBirthdayTodayValueYear" : "ProfileBirthdayValueYear", age, text);
                        } else {
                            text = LocaleController.formatString(today ? R.string.ProfileBirthdayTodayValue : R.string.ProfileBirthdayValue, text);
                        }

                        detailCell.setTextAndValue(
                                Emoji.replaceWithRestrictedEmoji(text, detailCell.textView, () -> {
                                    if (holder.getAdapterPosition() == position && profileActivity.birthdayRow == position && holder.getItemViewType() == VIEW_TYPE_TEXT_DETAIL) {
                                        onBindViewHolder(holder, position);
                                    }
                                }),
                                LocaleController.getString(today ? R.string.ProfileBirthdayToday : R.string.ProfileBirthday),
                                profileActivity.isTopic || profileActivity.bizHoursRow != -1 || profileActivity.bizLocationRow != -1
                        );

                        containsGift = !profileActivity.myProfile && today && !profileActivity.getMessagesController().premiumPurchaseBlocked();
                    }
                } else if (position == profileActivity.phoneRow) {
                    String text;
                    TLRPC.User user = profileActivity.getMessagesController().getUser(profileActivity.userId);
                    String phoneNumber;
                    if (user != null && !TextUtils.isEmpty(profileActivity.vcardPhone)) {
                        text = PhoneFormat.getInstance().format("+" + profileActivity.vcardPhone);
                        phoneNumber = profileActivity.vcardPhone;
                    } else if (user != null && !TextUtils.isEmpty(user.phone)) {
                        text = PhoneFormat.getInstance().format("+" + user.phone);
                        phoneNumber = user.phone;
                    } else {
                        text = LocaleController.getString(R.string.PhoneHidden);
                        phoneNumber = null;
                    }
                    profileActivity.isFragmentPhoneNumber = phoneNumber != null && phoneNumber.matches("888\\d{8}");
                    detailCell.setTextAndValue(text, LocaleController.getString(profileActivity.isFragmentPhoneNumber ? R.string.AnonymousNumber : R.string.PhoneMobile), false);
                } else if (position == profileActivity.usernameRow) {
                    String username = null;
                    CharSequence text;
                    CharSequence value;
                    ArrayList<TLRPC.TL_username> usernames = new ArrayList<>();
                    if (profileActivity.userId != 0) {
                        final TLRPC.User user = profileActivity.getMessagesController().getUser(profileActivity.userId);
                        if (user != null) {
                            usernames.addAll(user.usernames);
                        }
                        TLRPC.TL_username usernameObj = null;
                        if (user != null && !TextUtils.isEmpty(user.username)) {
                            usernameObj = DialogObject.findUsername(user.username, usernames);
                            username = user.username;
                        }
                        usernames = user == null ? new ArrayList<>() : new ArrayList<>(user.usernames);
                        if (TextUtils.isEmpty(username) && usernames != null) {
                            for (int i = 0; i < usernames.size(); ++i) {
                                TLRPC.TL_username u = usernames.get(i);
                                if (u != null && u.active && !TextUtils.isEmpty(u.username)) {
                                    usernameObj = u;
                                    username = u.username;
                                    break;
                                }
                            }
                        }
                        value = LocaleController.getString(R.string.Username);
                        if (username != null) {
                            text = "@" + username;
                            if (usernameObj != null && !usernameObj.editable) {
                                text = new SpannableString(text);
                                ((SpannableString) text).setSpan(makeUsernameLinkSpan(usernameObj), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        } else {
                            text = "—";
                        }
                        containsQr = true;
                    } else if (profileActivity.currentChat != null) {
                        TLRPC.Chat chat = profileActivity.getMessagesController().getChat(profileActivity.chatId);
                        username = ChatObject.getPublicUsername(chat);
                        if (chat != null) {
                            usernames.addAll(chat.usernames);
                        }
                        if (ChatObject.isPublic(chat)) {
                            containsQr = true;
                            text = profileActivity.getMessagesController().linkPrefix + "/" + username + (profileActivity.topicId != 0 ? "/" + profileActivity.topicId : "");
                            value = LocaleController.getString(R.string.InviteLink);
                        } else {
                            text = profileActivity.getMessagesController().linkPrefix + "/c/" + profileActivity.chatId + (profileActivity.topicId != 0 ? "/" + profileActivity.topicId : "");
                            value = LocaleController.getString(R.string.InviteLinkPrivate);
                        }
                    } else {
                        text = "";
                        value = "";
                        usernames = new ArrayList<>();
                    }
                    detailCell.setTextAndValue(text, alsoUsernamesString(username, usernames, value), (profileActivity.isTopic || profileActivity.bizHoursRow != -1 || profileActivity.bizLocationRow != -1) && profileActivity.birthdayRow < 0);
                } else if (position == profileActivity.locationRow) {
                    if (profileActivity.chatInfo != null && profileActivity.chatInfo.location instanceof TLRPC.TL_channelLocation) {
                        TLRPC.TL_channelLocation location = (TLRPC.TL_channelLocation) profileActivity.chatInfo.location;
                        detailCell.setTextAndValue(location.address, LocaleController.getString(R.string.AttachLocation), false);
                    }
                } else if (position == profileActivity.numberRow) {
                    TLRPC.User user = UserConfig.getInstance(profileActivity.getCurrentAccount()).getCurrentUser();
                    String value;
                    if (user != null && user.phone != null && user.phone.length() != 0) {
                        value = PhoneFormat.getInstance().format("+" + user.phone);
                    } else {
                        value = LocaleController.getString(R.string.NumberUnknown);
                    }
                    detailCell.setTextAndValue(value, LocaleController.getString(R.string.TapToChangePhone), true);
                    detailCell.setContentDescriptionValueFirst(false);
                } else if (position == profileActivity.setUsernameRow) {
                    TLRPC.User user = UserConfig.getInstance(profileActivity.getCurrentAccount()).getCurrentUser();
                    String text = "";
                    CharSequence value = LocaleController.getString(R.string.Username);
                    String username = null;
                    if (user != null && user.usernames.size() > 0) {
                        for (int i = 0; i < user.usernames.size(); ++i) {
                            TLRPC.TL_username u = user.usernames.get(i);
                            if (u != null && u.active && !TextUtils.isEmpty(u.username)) {
                                username = u.username;
                                break;
                            }
                        }
                        if (username == null) {
                            username = user.username;
                        }
                        if (username == null || TextUtils.isEmpty(username)) {
                            text = LocaleController.getString(R.string.UsernameEmpty);
                        } else {
                            text = "@" + username;
                        }
                        value = alsoUsernamesString(username, user.usernames, value);
                    } else {
                        username = UserObject.getPublicUsername(user);
                        if (user != null && !TextUtils.isEmpty(username)) {
                            text = "@" + username;
                        } else {
                            text = LocaleController.getString(R.string.UsernameEmpty);
                        }
                    }
                    detailCell.setTextAndValue(text, value, true);
                    detailCell.setContentDescriptionValueFirst(true);
                }
                if (containsGift) {
                    Drawable drawable = ContextCompat.getDrawable(detailCell.getContext(), R.drawable.msg_input_gift);
                    drawable.setColorFilter(new PorterDuffColorFilter(profileActivity.dontApplyPeerColor(profileActivity.getThemedColor(Theme.key_switch2TrackChecked), false), PorterDuff.Mode.MULTIPLY));
                    if (UserObject.areGiftsDisabled(profileActivity.userInfo)) {
                        detailCell.setImage(null);
                        detailCell.setImageClickListener(null);
                    } else {
                        detailCell.setImage(drawable, LocaleController.getString(R.string.GiftPremium));
                        detailCell.setImageClickListener(profileActivity::onTextDetailCellImageClicked);
                    }
                } else if (containsQr) {
                    Drawable drawable = ContextCompat.getDrawable(detailCell.getContext(), R.drawable.msg_qr_mini);
                    drawable.setColorFilter(new PorterDuffColorFilter(profileActivity.dontApplyPeerColor(profileActivity.getThemedColor(Theme.key_switch2TrackChecked), false), PorterDuff.Mode.MULTIPLY));
                    detailCell.setImage(drawable, LocaleController.getString(R.string.GetQRCode));
                    detailCell.setImageClickListener(profileActivity::onTextDetailCellImageClicked);
                } else {
                    detailCell.setImage(null);
                    detailCell.setImageClickListener(null);
                }
                detailCell.setTag(position);
                detailCell.textView.setLoading(profileActivity.loadingSpan);
                detailCell.valueTextView.setLoading(profileActivity.loadingSpan);
                break;
            case VIEW_TYPE_ABOUT_LINK:
                AboutLinkCell aboutLinkCell = (AboutLinkCell) holder.itemView;
                if (position == profileActivity.userInfoRow) {
                    TLRPC.User user = profileActivity.userInfo.user != null ? profileActivity.userInfo.user : profileActivity.getMessagesController().getUser(profileActivity.userInfo.id);
                    boolean addlinks = profileActivity.isBot || (user != null && user.premium && profileActivity.userInfo.about != null);
                    aboutLinkCell.setTextAndValue(profileActivity.userInfo.about, LocaleController.getString(R.string.UserBio), addlinks);
                } else if (position == profileActivity.channelInfoRow) {
                    String text = profileActivity.chatInfo.about;
                    while (text.contains("\n\n\n")) {
                        text = text.replace("\n\n\n", "\n\n");
                    }
                    aboutLinkCell.setText(text, ChatObject.isChannel(profileActivity.currentChat) && !profileActivity.currentChat.megagroup);
                } else if (position == profileActivity.bioRow) {
                    String value;
                    if (profileActivity.userInfo == null || !TextUtils.isEmpty(profileActivity.userInfo.about)) {
                        value = profileActivity.userInfo == null ? LocaleController.getString(R.string.Loading) : profileActivity.userInfo.about;
                        aboutLinkCell.setTextAndValue(value, LocaleController.getString(R.string.UserBio), profileActivity.getUserConfig().isPremium());
                        profileActivity.currentBio = profileActivity.userInfo != null ? profileActivity.userInfo.about : null;
                    } else {
                        aboutLinkCell.setTextAndValue(LocaleController.getString(R.string.UserBio), LocaleController.getString(R.string.UserBioDetail), false);
                        profileActivity.currentBio = null;
                    }
                    aboutLinkCell.setMoreButtonDisabled(true);
                }
                break;
            case VIEW_TYPE_PREMIUM_TEXT_CELL:
            case VIEW_TYPE_STARS_TEXT_CELL:
            case VIEW_TYPE_TEXT:
                TextCell textCell = (TextCell) holder.itemView;
                textCell.setColors(Theme.key_windowBackgroundWhiteGrayIcon, Theme.key_windowBackgroundWhiteBlackText);
                textCell.setTag(Theme.key_windowBackgroundWhiteBlackText);
                if (position == profileActivity.settingsTimerRow) {
                    TLRPC.EncryptedChat encryptedChat = profileActivity.getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(profileActivity.dialogId));
                    String value;
                    if (encryptedChat.ttl == 0) {
                        value = LocaleController.getString(R.string.ShortMessageLifetimeForever);
                    } else {
                        value = LocaleController.formatTTLString(encryptedChat.ttl);
                    }
                    textCell.setTextAndValue(LocaleController.getString(R.string.MessageLifetime), value, false, false);
                } else if (position == profileActivity.unblockRow) {
                    textCell.setText(LocaleController.getString(R.string.Unblock), false);
                    textCell.setColors(-1, Theme.key_text_RedRegular);
                } else if (position == profileActivity.settingsKeyRow) {
                    IdenticonDrawable identiconDrawable = new IdenticonDrawable();
                    TLRPC.EncryptedChat encryptedChat = profileActivity.getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(profileActivity.dialogId));
                    identiconDrawable.setEncryptedChat(encryptedChat);
                    textCell.setTextAndValueDrawable(LocaleController.getString(R.string.EncryptionKey), identiconDrawable, false);
                } else if (position == profileActivity.joinRow) {
                    textCell.setColors(-1, Theme.key_windowBackgroundWhiteBlueText2);
                    if (profileActivity.currentChat.megagroup) {
                        textCell.setText(LocaleController.getString(R.string.ProfileJoinGroup), false);
                    } else {
                        textCell.setText(LocaleController.getString(R.string.ProfileJoinChannel), false);
                    }
                } else if (position == profileActivity.subscribersRow) {
                    if (profileActivity.chatInfo != null) {
                        if (ChatObject.isChannel(profileActivity.currentChat) && !profileActivity.currentChat.megagroup) {
                            textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ChannelSubscribers), LocaleController.formatNumber(profileActivity.chatInfo.participants_count, ','), R.drawable.msg_groups, position != profileActivity.membersSectionRow - 1);
                        } else {
                            textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ChannelMembers), LocaleController.formatNumber(profileActivity.chatInfo.participants_count, ','), R.drawable.msg_groups, position != profileActivity.membersSectionRow - 1);
                        }
                    } else {
                        if (ChatObject.isChannel(profileActivity.currentChat) && !profileActivity.currentChat.megagroup) {
                            textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelSubscribers), R.drawable.msg_groups, position != profileActivity.membersSectionRow - 1);
                        } else {
                            textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelMembers), R.drawable.msg_groups, position != profileActivity.membersSectionRow - 1);
                        }
                    }
                } else if (position == profileActivity.subscribersRequestsRow) {
                    if (profileActivity.chatInfo != null) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.SubscribeRequests), String.format("%d", profileActivity.chatInfo.requests_pending), R.drawable.msg_requests, position != profileActivity.membersSectionRow - 1);
                    }
                } else if (position == profileActivity.administratorsRow) {
                    if (profileActivity.chatInfo != null) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ChannelAdministrators), String.format("%d", profileActivity.chatInfo.admins_count), R.drawable.msg_admins, position != profileActivity.membersSectionRow - 1);
                    } else {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelAdministrators), R.drawable.msg_admins, position != profileActivity.membersSectionRow - 1);
                    }
                } else if (position == profileActivity.settingsRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelAdminSettings), R.drawable.msg_customize, position != profileActivity.membersSectionRow - 1);
                } else if (position == profileActivity.channelBalanceRow) {
                    final TL_stars.StarsAmount stars_balance = BotStarsController.getInstance(profileActivity.getCurrentAccount()).getBotStarsBalance(-profileActivity.chatId);
                    final long ton_balance = BotStarsController.getInstance(profileActivity.getCurrentAccount()).getTONBalance(-profileActivity.chatId);
                    SpannableStringBuilder ssb = new SpannableStringBuilder();
                    if (ton_balance > 0) {
                        if (ton_balance / 1_000_000_000.0 > 1000.0) {
                            ssb.append("TON ").append(AndroidUtilities.formatWholeNumber((int) (ton_balance / 1_000_000_000.0), 0));
                        } else {
                            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                            symbols.setDecimalSeparator('.');
                            DecimalFormat formatterTON = new DecimalFormat("#.##", symbols);
                            formatterTON.setMinimumFractionDigits(2);
                            formatterTON.setMaximumFractionDigits(3);
                            formatterTON.setGroupingUsed(false);
                            ssb.append("TON ").append(formatterTON.format(ton_balance / 1_000_000_000.0));
                        }
                    }
                    if (stars_balance.amount > 0) {
                        if (ssb.length() > 0) ssb.append(" ");
                        ssb.append("XTR ").append(formatStarsAmountShort(stars_balance));
                    }
                    textCell.setTextAndValueAndIcon(getString(R.string.ChannelStars), ChannelMonetizationLayout.replaceTON(StarsIntroActivity.replaceStarsWithPlain(ssb, .7f), textCell.getTextView().getPaint()), R.drawable.menu_feature_paid, true);
                } else if (position == profileActivity.botStarsBalanceRow) {
                    final TL_stars.StarsAmount stars_balance = BotStarsController.getInstance(profileActivity.getCurrentAccount()).getBotStarsBalance(profileActivity.userId);
                    SpannableStringBuilder ssb = new SpannableStringBuilder();
                    if (stars_balance.amount > 0) {
                        ssb.append("XTR ").append(formatStarsAmountShort(stars_balance));
                    }
                    textCell.setTextAndValueAndIcon(getString(R.string.BotBalanceStars), ChannelMonetizationLayout.replaceTON(StarsIntroActivity.replaceStarsWithPlain(ssb, .7f), textCell.getTextView().getPaint()), R.drawable.menu_premium_main, true);
                } else if (position == profileActivity.botTonBalanceRow) {
                    long ton_balance = BotStarsController.getInstance(profileActivity.getCurrentAccount()).getTONBalance(profileActivity.userId);
                    SpannableStringBuilder ssb = new SpannableStringBuilder();
                    if (ton_balance > 0) {
                        if (ton_balance / 1_000_000_000.0 > 1000.0) {
                            ssb.append("TON ").append(AndroidUtilities.formatWholeNumber((int) (ton_balance / 1_000_000_000.0), 0));
                        } else {
                            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                            symbols.setDecimalSeparator('.');
                            DecimalFormat formatterTON = new DecimalFormat("#.##", symbols);
                            formatterTON.setMinimumFractionDigits(2);
                            formatterTON.setMaximumFractionDigits(3);
                            formatterTON.setGroupingUsed(false);
                            ssb.append("TON ").append(formatterTON.format(ton_balance / 1_000_000_000.0));
                        }
                    }
                    textCell.setTextAndValueAndIcon(getString(R.string.BotBalanceTON), ChannelMonetizationLayout.replaceTON(StarsIntroActivity.replaceStarsWithPlain(ssb, .7f), textCell.getTextView().getPaint()), R.drawable.msg_ton, true);
                } else if (position == profileActivity.blockedUsersRow) {
                    if (profileActivity.chatInfo != null) {
                        textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.ChannelBlacklist), String.format("%d", Math.max(profileActivity.chatInfo.banned_count, profileActivity.chatInfo.kicked_count)), R.drawable.msg_user_remove, position != profileActivity.membersSectionRow - 1);
                    } else {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.ChannelBlacklist), R.drawable.msg_user_remove, position != profileActivity.membersSectionRow - 1);
                    }
                } else if (position == profileActivity.addMemberRow) {
                    textCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                    boolean isNextPositionMember = position + 1 >= profileActivity.membersStartRow && position + 1 < profileActivity.membersEndRow;
                    textCell.setTextAndIcon(LocaleController.getString(R.string.AddMember), R.drawable.msg_contact_add, profileActivity.membersSectionRow == -1 || isNextPositionMember);
                } else if (position == profileActivity.sendMessageRow) {
                    textCell.setText(LocaleController.getString(R.string.SendMessageLocation), true);
                } else if (position == profileActivity.addToContactsRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.AddToContacts), R.drawable.msg_contact_add, false);
                    textCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                } else if (position == profileActivity.reportReactionRow) {
                    TLRPC.Chat chat = profileActivity.getMessagesController().getChat(-profileActivity.reportReactionFromDialogId);
                    if (chat != null && ChatObject.canBlockUsers(chat)) {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.ReportReactionAndBan), R.drawable.msg_block2, false);
                    } else {
                        textCell.setTextAndIcon(LocaleController.getString(R.string.ReportReaction), R.drawable.msg_report, false);
                    }

                    textCell.setColors(Theme.key_text_RedBold, Theme.key_text_RedRegular);
                    textCell.setColors(Theme.key_text_RedBold, Theme.key_text_RedRegular);
                } else if (position == profileActivity.reportRow) {
                    textCell.setText(LocaleController.getString(R.string.ReportUserLocation), false);
                    textCell.setColors(-1, Theme.key_text_RedRegular);
                    textCell.setColors(-1, Theme.key_text_RedRegular);
                } else if (position == profileActivity.languageRow) {
                    textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.Language), LocaleController.getCurrentLanguageName(), false, R.drawable.msg2_language, false);
                    textCell.setImageLeft(23);
                } else if (position == profileActivity.notificationRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.NotificationsAndSounds), R.drawable.msg2_notifications, true);
                } else if (position == profileActivity.privacyRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.PrivacySettings), R.drawable.msg2_secret, true);
                } else if (position == profileActivity.dataRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.DataSettings), R.drawable.msg2_data, true);
                } else if (position == profileActivity.chatRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.ChatSettings), R.drawable.msg2_discussion, true);
                } else if (position == profileActivity.filtersRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.Filters), R.drawable.msg2_folder, true);
                } else if (position == profileActivity.stickersRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.StickersName), R.drawable.msg2_sticker, true);
                } else if (position == profileActivity.liteModeRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.PowerUsage), R.drawable.msg2_battery, true);
                } else if (position == profileActivity.questionRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.AskAQuestion), R.drawable.msg2_ask_question, true);
                } else if (position == profileActivity.faqRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.TelegramFAQ), R.drawable.msg2_help, true);
                } else if (position == profileActivity.policyRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.PrivacyPolicy), R.drawable.msg2_policy, false);
                } else if (position == profileActivity.sendLogsRow) {
                    textCell.setText(LocaleController.getString(R.string.DebugSendLogs), true);
                } else if (position == profileActivity.sendLastLogsRow) {
                    textCell.setText(LocaleController.getString(R.string.DebugSendLastLogs), true);
                } else if (position == profileActivity.clearLogsRow) {
                    textCell.setText(LocaleController.getString(R.string.DebugClearLogs), profileActivity.switchBackendRow != -1);
                } else if (position == profileActivity.switchBackendRow) {
                    textCell.setText("Switch Backend", false);
                } else if (position == profileActivity.devicesRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.Devices), R.drawable.msg2_devices, true);
                } else if (position == profileActivity.setAvatarRow) {
                    profileActivity.cellCameraDrawable.setCustomEndFrame(86);
                    profileActivity.cellCameraDrawable.setCurrentFrame(85, false);
                    textCell.setTextAndIcon(LocaleController.getString(R.string.SetProfilePhoto), profileActivity.cellCameraDrawable, false);
                    textCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                    textCell.getImageView().setPadding(0, 0, 0, AndroidUtilities.dp(8));
                    textCell.setImageLeft(12);
                    profileActivity.setAvatarCell = textCell;
                } else if (position == profileActivity.addToGroupButtonRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.AddToGroupOrChannel), R.drawable.msg_groups_create, false);
                } else if (position == profileActivity.premiumRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.TelegramPremium), new AnimatedEmojiDrawable.WrapSizeDrawable(PremiumGradient.getInstance().premiumStarMenuDrawable, dp(24), dp(24)), true);
                    textCell.setImageLeft(23);
                } else if (position == profileActivity.starsRow) {
                    StarsController c = StarsController.getInstance(profileActivity.getCurrentAccount());
                    long balance = c.getBalance().amount;
                    textCell.setTextAndValueAndIcon(LocaleController.getString(R.string.MenuTelegramStars), c.balanceAvailable() && balance > 0 ? StarsIntroActivity.formatStarsAmount(c.getBalance(), 0.85f, ' ') : "", new AnimatedEmojiDrawable.WrapSizeDrawable(PremiumGradient.getInstance().goldenStarMenuDrawable, dp(24), dp(24)), true);
                    textCell.setImageLeft(23);
                } else if (position == profileActivity.tonRow) {
                    StarsController c = StarsController.getTonInstance(profileActivity.getCurrentAccount());
                    long balance = c.getBalance().amount;
                    textCell.setTextAndValueAndIcon(getString(R.string.MyTON), c.balanceAvailable() && balance > 0 ? StarsIntroActivity.formatStarsAmount(c.getBalance(), 0.85f, ' ') : "", R.drawable.menu_my_ton, true);
                    textCell.setImageLeft(23);
                } else if (position == profileActivity.businessRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.TelegramBusiness), R.drawable.menu_shop, true);
                    textCell.setImageLeft(23);
                } else if (position == profileActivity.premiumGiftingRow) {
                    textCell.setTextAndIcon(LocaleController.getString(R.string.SendAGift), R.drawable.menu_gift, false);
                    textCell.setImageLeft(23);
                } else if (position == profileActivity.botPermissionLocation) {
                    textCell.setTextAndCheckAndColorfulIcon(LocaleController.getString(R.string.BotProfilePermissionLocation), profileActivity.botLocation != null && profileActivity.botLocation.granted(), R.drawable.filled_access_location, profileActivity.getThemedColor(Theme.key_color_green), profileActivity.botPermissionBiometry != -1);
                } else if (position == profileActivity.botPermissionBiometry) {
                    textCell.setTextAndCheckAndColorfulIcon(LocaleController.getString(R.string.BotProfilePermissionBiometry), profileActivity.botBiometry != null && profileActivity.botBiometry.granted(), R.drawable.filled_access_fingerprint, profileActivity.getThemedColor(Theme.key_color_orange), false);
                } else if (position == profileActivity.botPermissionEmojiStatus) {
                    textCell.setTextAndCheckAndColorfulIcon(LocaleController.getString(R.string.BotProfilePermissionEmojiStatus), profileActivity.userInfo != null && profileActivity.userInfo.bot_can_manage_emoji_status, R.drawable.filled_access_sleeping, profileActivity.getThemedColor(Theme.key_color_lightblue), profileActivity.botPermissionLocation != -1 || profileActivity.botPermissionBiometry != -1);
                }
                textCell.valueTextView.setTextColor(profileActivity.dontApplyPeerColor(profileActivity.getThemedColor(Theme.key_windowBackgroundWhiteValueText), false));
                break;
            case VIEW_TYPE_NOTIFICATIONS_CHECK:
                NotificationsCheckCell checkCell = (NotificationsCheckCell) holder.itemView;
                if (position == profileActivity.notificationsRow) {
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(profileActivity.getCurrentAccount());
                    long did;
                    if (profileActivity.dialogId != 0) {
                        did = profileActivity.dialogId;
                    } else if (profileActivity.userId != 0) {
                        did = profileActivity.userId;
                    } else {
                        did = -profileActivity.chatId;
                    }
                    String key = NotificationsController.getSharedPrefKey(did, profileActivity.topicId);
                    boolean enabled = false;
                    boolean custom = preferences.getBoolean("custom_" + key, false);
                    boolean hasOverride = preferences.contains("notify2_" + key);
                    int value = preferences.getInt("notify2_" + key, 0);
                    int delta = preferences.getInt("notifyuntil_" + key, 0);
                    String val;
                    if (value == 3 && delta != Integer.MAX_VALUE) {
                        delta -= profileActivity.getConnectionsManager().getCurrentTime();
                        if (delta <= 0) {
                            if (custom) {
                                val = LocaleController.getString(R.string.NotificationsCustom);
                            } else {
                                val = LocaleController.getString(R.string.NotificationsOn);
                            }
                            enabled = true;
                        } else if (delta < 60 * 60) {
                            val = formatString("WillUnmuteIn", R.string.WillUnmuteIn, LocaleController.formatPluralString("Minutes", delta / 60));
                        } else if (delta < 60 * 60 * 24) {
                            val = formatString("WillUnmuteIn", R.string.WillUnmuteIn, LocaleController.formatPluralString("Hours", (int) Math.ceil(delta / 60.0f / 60)));
                        } else if (delta < 60 * 60 * 24 * 365) {
                            val = formatString("WillUnmuteIn", R.string.WillUnmuteIn, LocaleController.formatPluralString("Days", (int) Math.ceil(delta / 60.0f / 60 / 24)));
                        } else {
                            val = null;
                        }
                    } else {
                        if (value == 0) {
                            if (hasOverride) {
                                enabled = true;
                            } else {
                                enabled = profileActivity.getAccountInstance().getNotificationsController().isGlobalNotificationsEnabled(did, false, false);
                            }
                        } else if (value == 1) {
                            enabled = true;
                        }
                        if (enabled && custom) {
                            val = LocaleController.getString(R.string.NotificationsCustom);
                        } else {
                            val = enabled ? LocaleController.getString(R.string.NotificationsOn) : LocaleController.getString(R.string.NotificationsOff);
                        }
                    }
                    if (val == null) {
                        val = LocaleController.getString(R.string.NotificationsOff);
                    }
                    if (profileActivity.notificationsExceptionTopics != null && !profileActivity.notificationsExceptionTopics.isEmpty()) {
                        val = String.format(Locale.US, LocaleController.getPluralString("NotificationTopicExceptionsDesctription", profileActivity.notificationsExceptionTopics.size()), val, profileActivity.notificationsExceptionTopics.size());
                    }
                    checkCell.setAnimationsEnabled(profileActivity.fragmentOpened);
                    checkCell.setTextAndValueAndCheck(LocaleController.getString(R.string.Notifications), val, enabled, profileActivity.botAppRow >= 0);
                }
                break;
            case VIEW_TYPE_SHADOW:
                View sectionCell = holder.itemView;
                sectionCell.setTag(position);
                Drawable drawable;
                if (position == profileActivity.infoSectionRow && profileActivity.lastSectionRow == -1 && profileActivity.secretSettingsSectionRow == -1 && profileActivity.sharedMediaRow == -1 && profileActivity.membersSectionRow == -1 || position == profileActivity.secretSettingsSectionRow || position == profileActivity.lastSectionRow || position == profileActivity.membersSectionRow && profileActivity.lastSectionRow == -1 && profileActivity.sharedMediaRow == -1) {
                    sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, profileActivity.getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                } else {
                    sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, profileActivity.getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                }
                break;
            case VIEW_TYPE_SHADOW_TEXT: {
                TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                cell.setLinkTextRippleColor(null);
                if (position == profileActivity.infoSectionRow) {
                    final long did = profileActivity.getDialogId();
                    TLObject obj = profileActivity.getMessagesController().getUserOrChat(did);
                    TL_bots.botVerification bot_verification = profileActivity.userInfo != null ? profileActivity.userInfo.bot_verification : profileActivity.chatInfo != null ? profileActivity.chatInfo.bot_verification : null;
                    if (profileActivity.botAppRow >= 0 || bot_verification != null) {
                        cell.setFixedSize(0);
                        final TLRPC.User user = profileActivity.getMessagesController().getUser(profileActivity.userId);
                        final boolean botOwner = user != null && user.bot && user.bot_can_edit;
                        SpannableStringBuilder sb = new SpannableStringBuilder();

                        if (profileActivity.botAppRow >= 0) {
                            sb.append(AndroidUtilities.replaceSingleTag(getString(botOwner ? R.string.ProfileBotOpenAppInfoOwner : R.string.ProfileBotOpenAppInfo), () -> {
                                Browser.openUrl(profileActivity.getContext(), getString(botOwner ? R.string.ProfileBotOpenAppInfoOwnerLink : R.string.ProfileBotOpenAppInfoLink));
                            }));
                            if (bot_verification != null) {
                                sb.append("\n\n\n");
                            }
                        }
                        if (bot_verification != null) {
                            sb.append("x");
                            sb.setSpan(new AnimatedEmojiSpan(bot_verification.icon, cell.getTextView().getPaint().getFontMetricsInt()), sb.length() - 1, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            sb.append(" ");
                            SpannableString description = new SpannableString(bot_verification.description);
                            try {
                                AndroidUtilities.addLinksSafe(description, Linkify.WEB_URLS, false, false);
                                URLSpan[] spans = description.getSpans(0, description.length(), URLSpan.class);
                                for (int i = 0; i < spans.length; ++i) {
                                    URLSpan span = spans[i];
                                    int start = description.getSpanStart(span);
                                    int end = description.getSpanEnd(span);
                                    final String url = span.getURL();

                                    description.removeSpan(span);
                                    description.setSpan(new URLSpan(url) {
                                        @Override
                                        public void onClick(View widget) {
                                            Browser.openUrl(profileActivity.getContext(), url);
                                        }

                                        @Override
                                        public void updateDrawState(@NonNull TextPaint ds) {
                                            ds.setUnderlineText(true);
                                        }
                                    }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                            sb.append(description);
                        }

                        cell.setLinkTextRippleColor(Theme.multAlpha(profileActivity.getThemedColor(Theme.key_windowBackgroundWhiteGrayText4), 0.2f));
                        cell.setText(sb);
                    } else {
                        cell.setFixedSize(14);
                        cell.setText(null);
                    }
                } else if (position == profileActivity.infoAffiliateRow) {
                    final TLRPC.User botUser = profileActivity.getMessagesController().getUser(profileActivity.userId);
                    if (botUser != null && botUser.bot && botUser.bot_can_edit) {
                        cell.setFixedSize(0);
                        cell.setText(formatString(R.string.ProfileBotAffiliateProgramInfoOwner, UserObject.getUserName(botUser), percents(profileActivity.userInfo != null && profileActivity.userInfo.starref_program != null ? profileActivity.userInfo.starref_program.commission_permille : 0)));
                    } else {
                        cell.setFixedSize(0);
                        cell.setText(formatString(R.string.ProfileBotAffiliateProgramInfo, UserObject.getUserName(botUser), percents(profileActivity.userInfo != null && profileActivity.userInfo.starref_program != null ? profileActivity.userInfo.starref_program.commission_permille : 0)));
                    }
                }
                if (position == profileActivity.infoSectionRow && profileActivity.lastSectionRow == -1 && profileActivity.secretSettingsSectionRow == -1 && profileActivity.sharedMediaRow == -1 && profileActivity.membersSectionRow == -1 || position == profileActivity.secretSettingsSectionRow || position == profileActivity.lastSectionRow || position == profileActivity.membersSectionRow && profileActivity.lastSectionRow == -1 && profileActivity.sharedMediaRow == -1) {
                    cell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, profileActivity.getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                } else {
                    cell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, profileActivity.getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                }
                break;
            }
            case VIEW_TYPE_COLORFUL_TEXT: {
                AffiliateProgramFragment.ColorfulTextCell cell = (AffiliateProgramFragment.ColorfulTextCell) holder.itemView;
                cell.set(profileActivity.getThemedColor(Theme.key_color_green), R.drawable.filled_affiliate, getString(R.string.ProfileBotAffiliateProgram), null);
                cell.setPercent(profileActivity.userInfo != null && profileActivity.userInfo.starref_program != null ? percents(profileActivity.userInfo.starref_program.commission_permille) : null);
                break;
            }
            case VIEW_TYPE_USER:
                UserCell userCell = (UserCell) holder.itemView;
                TLRPC.ChatParticipant part;
                try {
                    if (!profileActivity.visibleSortedUsers.isEmpty()) {
                        part = profileActivity.visibleChatParticipants.get(profileActivity.visibleSortedUsers.get(position - profileActivity.membersStartRow));
                    } else {
                        part = profileActivity.visibleChatParticipants.get(position - profileActivity.membersStartRow);
                    }
                } catch (Exception e) {
                    part = null;
                    FileLog.e(e);
                }
                if (part != null) {
                    String role;
                    if (part instanceof TLRPC.TL_chatChannelParticipant) {
                        TLRPC.ChannelParticipant channelParticipant = ((TLRPC.TL_chatChannelParticipant) part).channelParticipant;
                        if (!TextUtils.isEmpty(channelParticipant.rank)) {
                            role = channelParticipant.rank;
                        } else {
                            if (channelParticipant instanceof TLRPC.TL_channelParticipantCreator) {
                                role = LocaleController.getString(R.string.ChannelCreator);
                            } else if (channelParticipant instanceof TLRPC.TL_channelParticipantAdmin) {
                                role = LocaleController.getString(R.string.ChannelAdmin);
                            } else {
                                role = null;
                            }
                        }
                    } else {
                        if (part instanceof TLRPC.TL_chatParticipantCreator) {
                            role = LocaleController.getString(R.string.ChannelCreator);
                        } else if (part instanceof TLRPC.TL_chatParticipantAdmin) {
                            role = getString(R.string.ChannelAdmin);
                        } else {
                            role = null;
                        }
                    }
                    userCell.setAdminRole(role);
                    userCell.setData(profileActivity.getMessagesController().getUser(part.user_id), null, null, 0, position != profileActivity.membersEndRow - 1);
                }
                break;
            case VIEW_TYPE_BOTTOM_PADDING:
                holder.itemView.requestLayout();
                break;
            case VIEW_TYPE_SUGGESTION:
                SettingsSuggestionCell suggestionCell = (SettingsSuggestionCell) holder.itemView;
                if (position == profileActivity.passwordSuggestionRow) {
                    suggestionCell.setType(SettingsSuggestionCell.TYPE_PASSWORD);
                } else if (position == profileActivity.phoneSuggestionRow) {
                    suggestionCell.setType(SettingsSuggestionCell.TYPE_PHONE);
                } else if (position == profileActivity.graceSuggestionRow) {
                    suggestionCell.setType(SettingsSuggestionCell.TYPE_GRACE);
                }
                break;
            case VIEW_TYPE_ADDTOGROUP_INFO:
                TextInfoPrivacyCell addToGroupInfo = (TextInfoPrivacyCell) holder.itemView;
                addToGroupInfo.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, profileActivity.getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                addToGroupInfo.setText(LocaleController.getString(R.string.BotAddToGroupOrChannelInfo));
                break;
            case VIEW_TYPE_NOTIFICATIONS_CHECK_SIMPLE:
                TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                textCheckCell.setTextAndCheck(LocaleController.getString(R.string.Notifications), !profileActivity.getMessagesController().isDialogMuted(profileActivity.getDialogId(), profileActivity.topicId), false);
                break;
            case VIEW_TYPE_LOCATION:
                ((ProfileLocationCell) holder.itemView).set(profileActivity.userInfo != null ? profileActivity.userInfo.business_location : null, profileActivity.notificationsDividerRow < 0 && !profileActivity.myProfile);
                break;
            case VIEW_TYPE_HOURS:
                ProfileHoursCell hoursCell = (ProfileHoursCell) holder.itemView;
                hoursCell.setOnTimezoneSwitchClick(view -> {
                    profileActivity.hoursShownMine = !profileActivity.hoursShownMine;
                    if (!profileActivity.hoursExpanded) {
                        profileActivity.hoursExpanded = true;
                    }
                    profileActivity.saveScrollPosition();
                    view.requestLayout();
                    profileActivity.listAdapter.notifyItemChanged(profileActivity.bizHoursRow);
                    if (profileActivity.savedScrollPosition >= 0) {
                        profileActivity.layoutManager.scrollToPositionWithOffset(profileActivity.savedScrollPosition, profileActivity.savedScrollOffset - profileActivity.listView.getPaddingTop());
                    }
                });
                hoursCell.set(profileActivity.userInfo != null ? profileActivity.userInfo.business_work_hours : null, profileActivity.hoursExpanded, profileActivity.hoursShownMine, profileActivity.notificationsDividerRow < 0 && !profileActivity.myProfile || profileActivity.bizLocationRow >= 0);
                break;
            case VIEW_TYPE_CHANNEL:
                ((ProfileChannelCell) holder.itemView).set(
                        profileActivity.getMessagesController().getChat(profileActivity.userInfo.personal_channel_id),
                        profileActivity.profileChannelMessageFetcher != null ? profileActivity.profileChannelMessageFetcher.messageObject : null
                );
                break;
            case VIEW_TYPE_BOT_APP:

                break;
        }
    }

    private CharSequence alsoUsernamesString(String originalUsername, ArrayList<TLRPC.TL_username> alsoUsernames, CharSequence fallback) {
        if (alsoUsernames == null) {
            return fallback;
        }
        alsoUsernames = new ArrayList<>(alsoUsernames);
        for (int i = 0; i < alsoUsernames.size(); ++i) {
            if (
                    !alsoUsernames.get(i).active ||
                            originalUsername != null && originalUsername.equals(alsoUsernames.get(i).username)
            ) {
                alsoUsernames.remove(i--);
            }
        }
        if (alsoUsernames.size() > 0) {
            SpannableStringBuilder usernames = new SpannableStringBuilder();
            for (int i = 0; i < alsoUsernames.size(); ++i) {
                TLRPC.TL_username usernameObj = alsoUsernames.get(i);
                final String usernameRaw = usernameObj.username;
                SpannableString username = new SpannableString("@" + usernameRaw);
                username.setSpan(makeUsernameLinkSpan(usernameObj), 0, username.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                username.setSpan(new ForegroundColorSpan(profileActivity.dontApplyPeerColor(profileActivity.getThemedColor(Theme.key_chat_messageLinkIn), false)), 0, username.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                usernames.append(username);
                if (i < alsoUsernames.size() - 1) {
                    usernames.append(", ");
                }
            }
            String string = getString(R.string.UsernameAlso);
            SpannableStringBuilder finalString = new SpannableStringBuilder(string);
            final String toFind = "%1$s";
            int index = string.indexOf(toFind);
            if (index >= 0) {
                finalString.replace(index, index + toFind.length(), usernames);
            }
            return finalString;
        } else {
            return fallback;
        }
    }

    private final HashMap<TLRPC.TL_username, ClickableSpan> usernameSpans = new HashMap<TLRPC.TL_username, ClickableSpan>();

    public ClickableSpan makeUsernameLinkSpan(TLRPC.TL_username usernameObj) {
        ClickableSpan span = usernameSpans.get(usernameObj);
        if (span != null) return span;

        final String usernameRaw = usernameObj.username;
        span = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                if (!usernameObj.editable) {
                    if (profileActivity.loadingSpan == this) return;
                    profileActivity.setLoadingSpan(this);
                    TL_fragment.TL_getCollectibleInfo req = new TL_fragment.TL_getCollectibleInfo();
                    TL_fragment.TL_inputCollectibleUsername input = new TL_fragment.TL_inputCollectibleUsername();
                    input.username = usernameObj.username;
                    req.collectible = input;
                    int reqId = profileActivity.getConnectionsManager().sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> {
                        profileActivity.setLoadingSpan(null);
                        if (res instanceof TL_fragment.TL_collectibleInfo) {
                            TLObject obj;
                            if (profileActivity.userId != 0) {
                                obj = profileActivity.getMessagesController().getUser(profileActivity.userId);
                            } else {
                                obj = profileActivity.getMessagesController().getChat(profileActivity.chatId);
                            }
                            if (profileActivity.getContext() == null) {
                                return;
                            }
                            FragmentUsernameBottomSheet.open(profileActivity.getContext(), FragmentUsernameBottomSheet.TYPE_USERNAME, usernameObj.username, obj, (TL_fragment.TL_collectibleInfo) res, profileActivity.getResourceProvider());
                        } else {
                            BulletinFactory.showError(err);
                        }
                    }));
                    profileActivity.getConnectionsManager().bindRequestToGuid(reqId, profileActivity.getClassGuid());
                } else {
                    profileActivity.setLoadingSpan(null);
                    String urlFinal = profileActivity.getMessagesController().linkPrefix + "/" + usernameRaw;
                    if (profileActivity.currentChat == null || !profileActivity.currentChat.noforwards) {
                        AndroidUtilities.addToClipboard(urlFinal);
                        profileActivity.undoView.showWithAction(0, UndoView.ACTION_USERNAME_COPIED, null);
                    }
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setColor(ds.linkColor);
            }
        };
        usernameSpans.put(usernameObj, span);
        return span;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder.getAdapterPosition() == profileActivity.setAvatarRow) {
            profileActivity.setAvatarCell = null;
        }
    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        if (profileActivity.notificationRow != -1) {
            int position = holder.getAdapterPosition();
            return position == profileActivity.notificationRow || position == profileActivity.numberRow || position == profileActivity.privacyRow ||
                    position == profileActivity.languageRow || position == profileActivity.setUsernameRow || position == profileActivity.bioRow ||
                    position == profileActivity.versionRow || position == profileActivity.dataRow || position == profileActivity.chatRow ||
                    position == profileActivity.questionRow || position == profileActivity.devicesRow || position == profileActivity.filtersRow || position == profileActivity.stickersRow ||
                    position == profileActivity.faqRow || position == profileActivity.policyRow || position == profileActivity.sendLogsRow || position == profileActivity.sendLastLogsRow ||
                    position == profileActivity.clearLogsRow || position == profileActivity.switchBackendRow || position == profileActivity.setAvatarRow ||
                    position == profileActivity.addToGroupButtonRow || position == profileActivity.premiumRow || position == profileActivity.premiumGiftingRow ||
                    position == profileActivity.businessRow || position == profileActivity.liteModeRow || position == profileActivity.birthdayRow || position == profileActivity.channelRow ||
                    position == profileActivity.starsRow || position == profileActivity.tonRow;
        }
        if (holder.itemView instanceof UserCell) {
            UserCell userCell = (UserCell) holder.itemView;
            Object object = userCell.getCurrentObject();
            if (object instanceof TLRPC.User) {
                TLRPC.User user = (TLRPC.User) object;
                if (UserObject.isUserSelf(user)) {
                    return false;
                }
            }
        }
        int type = holder.getItemViewType();
        return type != VIEW_TYPE_HEADER && type != VIEW_TYPE_DIVIDER && type != VIEW_TYPE_SHADOW &&
                type != VIEW_TYPE_EMPTY && type != VIEW_TYPE_BOTTOM_PADDING && type != VIEW_TYPE_SHARED_MEDIA &&
                type != 9 && type != 10 && type != VIEW_TYPE_BOT_APP; // These are legacy ones, left for compatibility
    }

    @Override
    public int getItemCount() {
        return profileActivity.rowCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == profileActivity.infoHeaderRow || position == profileActivity.membersHeaderRow || position == profileActivity.settingsSectionRow2 ||
                position == profileActivity.numberSectionRow || position == profileActivity.helpHeaderRow || position == profileActivity.debugHeaderRow || position == profileActivity.botPermissionsHeader) {
            return VIEW_TYPE_HEADER;
        } else if (position == profileActivity.phoneRow || position == profileActivity.locationRow || position == profileActivity.numberRow || position == profileActivity.birthdayRow) {
            return VIEW_TYPE_TEXT_DETAIL;
        } else if (position == profileActivity.usernameRow || position == profileActivity.setUsernameRow) {
            return VIEW_TYPE_TEXT_DETAIL_MULTILINE;
        } else if (position == profileActivity.userInfoRow || position == profileActivity.channelInfoRow || position == profileActivity.bioRow) {
            return VIEW_TYPE_ABOUT_LINK;
        } else if (position == profileActivity.settingsTimerRow || position == profileActivity.settingsKeyRow || position == profileActivity.reportRow || position == profileActivity.reportReactionRow ||
                position == profileActivity.subscribersRow || position == profileActivity.subscribersRequestsRow || position == profileActivity.administratorsRow || position == profileActivity.settingsRow || position == profileActivity.blockedUsersRow ||
                position == profileActivity.addMemberRow || position == profileActivity.joinRow || position == profileActivity.unblockRow ||
                position == profileActivity.sendMessageRow || position == profileActivity.notificationRow || position == profileActivity.privacyRow ||
                position == profileActivity.languageRow || position == profileActivity.dataRow || position == profileActivity.chatRow ||
                position == profileActivity.questionRow || position == profileActivity.devicesRow || position == profileActivity.filtersRow || position == profileActivity.stickersRow ||
                position == profileActivity.faqRow || position == profileActivity.policyRow || position == profileActivity.sendLogsRow || position == profileActivity.sendLastLogsRow ||
                position == profileActivity.clearLogsRow || position == profileActivity.switchBackendRow || position == profileActivity.setAvatarRow || position == profileActivity.addToGroupButtonRow ||
                position == profileActivity.addToContactsRow || position == profileActivity.liteModeRow || position == profileActivity.premiumGiftingRow || position == profileActivity.businessRow ||
                position == profileActivity.botStarsBalanceRow || position == profileActivity.botTonBalanceRow || position == profileActivity.channelBalanceRow || position == profileActivity.botPermissionLocation ||
                position == profileActivity.botPermissionBiometry || position == profileActivity.botPermissionEmojiStatus || position == profileActivity.tonRow
        ) {
            return VIEW_TYPE_TEXT;
        } else if (position == profileActivity.notificationsDividerRow) {
            return VIEW_TYPE_DIVIDER;
        } else if (position == profileActivity.notificationsRow) {
            return VIEW_TYPE_NOTIFICATIONS_CHECK;
        } else if (position == profileActivity.notificationsSimpleRow) {
            return VIEW_TYPE_NOTIFICATIONS_CHECK_SIMPLE;
        } else if (position == profileActivity.lastSectionRow || position == profileActivity.membersSectionRow ||
                position == profileActivity.secretSettingsSectionRow || position == profileActivity.settingsSectionRow || position == profileActivity.devicesSectionRow ||
                position == profileActivity.helpSectionCell || position == profileActivity.setAvatarSectionRow || position == profileActivity.passwordSuggestionSectionRow ||
                position == profileActivity.phoneSuggestionSectionRow || position == profileActivity.premiumSectionsRow || position == profileActivity.reportDividerRow ||
                position == profileActivity.channelDividerRow || position == profileActivity.graceSuggestionSectionRow || position == profileActivity.balanceDividerRow ||
                position == profileActivity.botPermissionsDivider || position == profileActivity.channelBalanceSectionRow
        ) {
            return VIEW_TYPE_SHADOW;
        } else if (position >= profileActivity.membersStartRow && position < profileActivity.membersEndRow) {
            return VIEW_TYPE_USER;
        } else if (position == profileActivity.emptyRow) {
            return VIEW_TYPE_EMPTY;
        } else if (position == profileActivity.bottomPaddingRow) {
            return VIEW_TYPE_BOTTOM_PADDING;
        } else if (position == profileActivity.sharedMediaRow) {
            return VIEW_TYPE_SHARED_MEDIA;
        } else if (position == profileActivity.versionRow) {
            return VIEW_TYPE_VERSION;
        } else if (position == profileActivity.passwordSuggestionRow || position == profileActivity.phoneSuggestionRow || position == profileActivity.graceSuggestionRow) {
            return VIEW_TYPE_SUGGESTION;
        } else if (position == profileActivity.addToGroupInfoRow) {
            return VIEW_TYPE_ADDTOGROUP_INFO;
        } else if (position == profileActivity.premiumRow) {
            return VIEW_TYPE_PREMIUM_TEXT_CELL;
        } else if (position == profileActivity.starsRow) {
            return VIEW_TYPE_STARS_TEXT_CELL;
        } else if (position == profileActivity.bizLocationRow) {
            return VIEW_TYPE_LOCATION;
        } else if (position == profileActivity.bizHoursRow) {
            return VIEW_TYPE_HOURS;
        } else if (position == profileActivity.channelRow) {
            return VIEW_TYPE_CHANNEL;
        } else if (position == profileActivity.botAppRow) {
            return VIEW_TYPE_BOT_APP;
        } else if (position == profileActivity.infoSectionRow || position == profileActivity.infoAffiliateRow) {
            return VIEW_TYPE_SHADOW_TEXT;
        } else if (position == profileActivity.affiliateRow) {
            return VIEW_TYPE_COLORFUL_TEXT;
        }
        return 0;
    }
}
