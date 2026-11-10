package com.example.data.repository

import com.example.data.model.DailyInspiration
import com.example.data.model.DuaCategory
import com.example.data.model.DuaItem

object DuasRepository {

    val dailyInspiration = DailyInspiration(
        verseArabic = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
        verseEn = "So remember Me; I will remember you. And be grateful to Me and do not deny Me.",
        verseSo = "I xusa aan idin xusee, ii mahadnaqa hana ku gaaloobina nicmadayda.",
        verseTr = "Öyleyse siz beni (ibadetle) anın ki ben de sizi anayım. Bana şükredin; sakın nankörlük etmeyin!",
        verseFr = "Souvenez-vous de Moi donc, Je vous récompenserai. Remerciez-Moi et ne soyez pas ingrats envers Moi.",
        verseRef = "Surah Al-Baqarah (2:152)",

        hadithArabic = "قَالَ رَسُولُ اللَّهِ ﷺ: «مَثَلُ الَّذِي يَذْكُرُ رَبَّهُ وَالَّذِي لا يَذْكُرُ رَبَّهُ مَثَلُ الحَيِّ وَالمَيِّتِ»",
        hadithEn = "The Messenger of Allah ﷺ said: 'The example of the one who remembers his Lord in comparison to the one who does not remember his Lord is that of the living and the dead.'",
        hadithSo = "Rasuulkii Eebbe ﷺ wuxuu yiri: 'Qofka Rabbigiis xusa iyo kan aan xusin waxay la mid yihiin noolaha iyo meydka.'",
        hadithTr = "Resûlullah ﷺ şöyle buyurdu: 'Rabbini zikreden ile zikretmeyenin misali, diri ile ölünün misali gibidir.'",
        hadithFr = "Le Messager d'Allah ﷺ a dit : 'L'exemple de celui qui invoque son Seigneur et de celui qui ne l'invoque pas est comme l'exemple du vivant et du mort.'",
        hadithRef = "Sahih al-Bukhari 6407"
    )

    val allDuas: List<DuaItem> = listOf(
        // MORNING & EVENING
        DuaItem(
            id = "dua_sayyid_istighfar",
            category = DuaCategory.MORNING_EVENING,
            titleEn = "Master Supplication for Forgiveness (Sayyid al-Istighfar)",
            titleSo = "Sayidka Dambi-dhaaf-dalabka (Sayidul Istiqfaar)",
            titleAr = "سيد الاستغفار",
            titleTr = "Seyyidü'l İstiğfar (Bağışlanma Duası)",
            titleFr = "La maîtresse demande de pardon (Sayyid al-Istighfar)",
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allahumma Anta Rabbi, la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu, a'udhu bika min sharri ma sana'tu, abu'u laka bini'matika 'alayya, wa abu'u bidhanbi faghfir li, fa-innahu la yaghfiru-dhunuba illa Anta.",
            translationEn = "O Allah, You are my Lord, there is none worthy of worship but You. You created me and I am Your slave. I abide by Your covenant and promise as best I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favor upon me and I acknowledge my sin, so forgive me, for none forgives sins except You.",
            translationSo = "Allow adigu waxaad tahay Rabbigay, ilaah xaq lagu caabudo ma jiro adiga mooyee, waad i abuurtay anna addoonkaaga baan ahay, axdigaaga iyo ballankaaga baan ku taaganahay inta karaankayga ah...",
            translationTr = "Allah'ım! Sen benim Rabbimsin. Senden başka hiçbir ilâh yoktur. Beni sen yarattın ve ben senin kulunum. Gücüm yettiğince sana verdiğim ahde ve vaade bağlıyım...",
            translationFr = "Ô Allah ! Tu es mon Seigneur, il n'y a de divinité digne d'adoration que Toi. Tu m'as créé et je suis Ton serviteur...",
            targetCount = 1,
            reference = "Sahih al-Bukhari 6306"
        ),
        DuaItem(
            id = "dua_morning_protection",
            category = DuaCategory.MORNING_EVENING,
            titleEn = "Supplication for Protection in the Morning & Evening",
            titleSo = "Ducada Ilaalinta Subaxdii iyo Galabtii",
            titleAr = "دعاء الحفظ والوقاية",
            titleTr = "Zararlardan Korunma Duası",
            titleFr = "Invocation de protection matin et soir",
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahil-ladhi la yadurru ma'as-mihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Alim.",
            translationEn = "In the name of Allah, with Whose name nothing can cause harm in the earth nor in the heavens, and He is the All-Hearing, the All-Knowing.",
            translationSo = "Magaca Eebaan ku bilaabayaa ee uusan waxba yeeli karin magaciisa wax dhulka ama samada jooga, waana Maqlaha Wax walba Og.",
            translationTr = "İsmiyle yerde ve gökte hiçbir şeyin zarar veremeyeceği Allah'ın adıyla! O hakkıyla işiten ve bilendir.",
            translationFr = "Au nom d'Allah, tel qu'en compagnie de Son Nom rien ne peut nuire sur terre ni dans le ciel, et Il est l'Audient, l'Omniscient.",
            targetCount = 3,
            reference = "Sunan Abi Dawud 5088, At-Tirmidhi 3388"
        ),
        DuaItem(
            id = "dua_evening_peace",
            category = DuaCategory.MORNING_EVENING,
            titleEn = "Evening Affirmation of Faith",
            titleSo = "Dikriga Galabta iyo Qirashada Iimaanka",
            titleAr = "أذكار المساء",
            titleTr = "Akşam Zikri",
            titleFr = "Invocation du soir",
            arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "Amsayna wa amsal-mulku lillah, wal-hamdu lillah, la ilaha illallahu wahdahu la sharika lahu, lahul-mulku wa lahul-hamdu, wa Huwa 'ala kulli shay'in qadir.",
            translationEn = "We have reached the evening and the kingdom belongs to Allah, and all praise is for Allah. There is none worthy of worship but Allah alone, without partner. His is the sovereignty and to Him is praise, and He is over all things capable.",
            translationSo = "Waxaan galabaysannay iyadoo boqortooyada oo dhan Eebbe leeyahay, mahadna Eebaa iska leh...",
            translationTr = "Akşama girdik; mülk de Allah için akşama erdi. Hamd Allah'a mahsustur...",
            translationFr = "Nous voici au soir et le royaume appartient à Allah. Louange à Allah...",
            targetCount = 1,
            reference = "Sahih Muslim 2723"
        ),

        // SLEEPING & WAKING
        DuaItem(
            id = "dua_before_sleeping",
            category = DuaCategory.SLEEP_WAKE,
            titleEn = "Before Sleeping",
            titleSo = "Ducada Hurdo Tagista",
            titleAr = "دعاء النوم",
            titleTr = "Uyumadan Önceki Dua",
            titleFr = "Invocation avant de dormir",
            arabicText = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            transliteration = "Bismika Allahumma amutu wa ahya.",
            translationEn = "In Your name, O Allah, I die and I live.",
            translationSo = "Magacaaga, Allow, ayaan ku dhintaa (seexdaa) kuna noolaadaa (soo toosaa).",
            translationTr = "Allah'ım! Senin isminle ölür (uyur) ve senin isminle dirilirim (uyanırım).",
            translationFr = "En Ton nom, ô Allah, je meurs et je vis.",
            targetCount = 1,
            reference = "Sahih al-Bukhari 6324"
        ),
        DuaItem(
            id = "dua_waking_up",
            category = DuaCategory.SLEEP_WAKE,
            titleEn = "Upon Waking Up",
            titleSo = "Ducada Soo Toosidda Hurdo",
            titleAr = "دعاء الاستيقاظ من النوم",
            titleTr = "Uykudan Uyanınca Okunacak Dua",
            titleFr = "Invocation au réveil",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliteration = "Alhamdu lillahil-ladhi ahyana ba'da ma amatana wa ilayhin-nushur.",
            translationEn = "All praise is for Allah Who gave us life after having taken it from us, and unto Him is the resurrection.",
            translationSo = "Mahad Eebaa iska leh ee na nooleeyay ka dib markuu na dilay, xaggiisana waa soo bixiddu.",
            translationTr = "Bizi öldükten (uyuduktan) sonra dirilten Allah'a hamdolsun. Dönüş de ancak O'nadır.",
            translationFr = "Louange à Allah qui nous a rendu la vie après nous avoir fait mourir, et vers Lui est la résurrection.",
            targetCount = 1,
            reference = "Sahih al-Bukhari 6312"
        ),

        // FOOD & DRINK
        DuaItem(
            id = "dua_before_eating",
            category = DuaCategory.FOOD_DRINK,
            titleEn = "Before Eating",
            titleSo = "Ducada Cuntada Ka Hor",
            titleAr = "دعاء قبل الأكل",
            titleTr = "Yemekten Önceki Dua",
            titleFr = "Avant de manger",
            arabicText = "بِسْمِ اللَّهِ، فَإِنْ نَسِيَ فَلْيَقُلْ: بِسْمِ اللَّهِ فِي أَوَّلِهِ وَآخِرِهِ",
            transliteration = "Bismillah (or: Bismillahi fi awwalihi wa akhirihi).",
            translationEn = "In the Name of Allah. (If forgotten initially, say: In the Name of Allah in its beginning and end).",
            translationSo = "Magaca Eebe. (Haddaad ilowdo ku bilow: Bismillaahi fi awwalihi wa aakhirih).",
            translationTr = "Allah'ın adıyla. (Unutulursa: Başında da sonunda da Allah'ın adıyla).",
            translationFr = "Au nom d'Allah. (En cas d'oubli : Au nom d'Allah au début et à la fin).",
            targetCount = 1,
            reference = "Sunan Abi Dawud 3767, At-Tirmidhi 1858"
        ),
        DuaItem(
            id = "dua_after_eating",
            category = DuaCategory.FOOD_DRINK,
            titleEn = "After Eating",
            titleSo = "Ducada Cuntada Ka Dib",
            titleAr = "دعاء الفراغ من الطعام",
            titleTr = "Yemekten Sonraki Dua",
            titleFr = "Après avoir mangé",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
            transliteration = "Alhamdu lillahil-ladhi at'amani hadha wa razaqanihi min ghayri hawlin minni wa la quwwah.",
            translationEn = "Praise be to Allah Who has fed me this and provided it for me without any might or power on my part.",
            translationSo = "Mahad Eebaa iska leh ee cuntadan i siiyay iina risaaqay anoon tabar iyo xoog lahayn.",
            translationTr = "Bana bu yemeği yediren ve tarafımdan hiçbir güç ve kuvvet olmadan bunu bana nasip eden Allah'a hamdolsun.",
            translationFr = "Louange à Allah qui m'a nourri de ceci et me l'a accordé sans force ni puissance de ma part.",
            targetCount = 1,
            reference = "Sunan At-Tirmidhi 3458"
        ),

        // TRAVEL
        DuaItem(
            id = "dua_travel",
            category = DuaCategory.TRAVEL,
            titleEn = "Supplication for Traveling & Journey",
            titleSo = "Ducada Socdaalka iyo Safarka",
            titleAr = "دعاء السفر",
            titleTr = "Yolculuk Duası",
            titleFr = "Invocation du voyageur",
            arabicText = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ. اللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَذَا الْبِرَّ وَالتَّقْوَى، وَمِنَ الْعَمَلِ مَا تَرْضَى",
            transliteration = "Subhanal-ladhi sakh-khara lana hadha wa ma kunna lahu muqrinin, wa inna ila Rabbina lamunqalibun. Allahumma inna nas'aluka fi safarina hadhal-birra wat-taqwa, wa minal-'amali ma tarda.",
            translationEn = "Exalted is He Who has subjected this to us, and we could not have otherwise subdued it. And indeed, to our Lord we will return. O Allah, we ask You on this journey for righteousness and piety, and for deeds that please You.",
            translationSo = "Waxaa nasahan Eebaha kan noo sakhiray iyadoonannan awood u lahayn, xagga Rabbigeenna ayaana u noqonaynaa...",
            translationTr = "Bunu bizim hizmetimize veren Allah'ın şânı ne yücedir! Yoksa biz buna güç yetiremezdik. Şüphesiz biz Rabbimize döneceğiz...",
            translationFr = "Gloire à Celui qui a mis ceci à notre disposition alors que nous n'étions pas capables de les dominer...",
            targetCount = 1,
            reference = "Sahih Muslim 1342"
        ),

        // PROTECTION & HEALING
        DuaItem(
            id = "dua_anxiety_distress",
            category = DuaCategory.PROTECTION,
            titleEn = "Relief from Anxiety and Distress",
            titleSo = "Ducada Welwelka iyo Murugada",
            titleAr = "دعاء إزالة الهم والحزن",
            titleTr = "Keder ve Üzüntü Anında Dua",
            titleFr = "Contre l'angoisse et la tristesse",
            arabicText = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْبُخْلِ وَالْجُبْنِ، وَضَلَعِ الدَّيْنِ، وَغَلَبَةِ الرِّجَالِ",
            transliteration = "Allahumma inni a'udhu bika minal-hammi wal-hazan, wal-'ajzi wal-kasal, wal-bukhli wal-jubn, wa dala'id-dayn, wa ghalabatir-rijal.",
            translationEn = "O Allah, I seek refuge in You from anxiety and grief, helplessness and laziness, miserliness and cowardice, the burden of debts, and being overpowered by men.",
            translationSo = "Allow waxaan kaa magangalayaa werwerka iyo murugada, caajiska iyo hawlgabnimada, bakhaylnimada iyo fuleynimada, culayska deynta, iyo cadaadiska ragga.",
            translationTr = "Allah'ım! Keder ve üzüntüden, acizlik ve tembellikten, cimrilik ve korkaklıktan, borç yükünden ve insanların kahrından sana sığınırım.",
            translationFr = "Ô Allah ! Je cherche refuge auprès de Toi contre les soucis et la tristesse, l'impuissance et la paresse, l'avarice et la lâcheté, le fardeau des dettes et la domination des hommes.",
            targetCount = 1,
            reference = "Sahih al-Bukhari 2893"
        ),

        // MOSQUE & PRAYER
        DuaItem(
            id = "dua_entering_mosque",
            category = DuaCategory.MOSQUE_PRAYER,
            titleEn = "Entering the Mosque",
            titleSo = "Ducada Gelitaanka Masaajidka",
            titleAr = "دعاء دخول المسجد",
            titleTr = "Camiye Girerken Okunacak Dua",
            titleFr = "En entrant à la mosquée",
            arabicText = "بِسْمِ اللَّهِ، وَالصَّلَاةُ وَالسَّلَامُ عَلَى رَسُولِ اللَّهِ، اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            transliteration = "Bismillah, was-salatu was-salamu 'ala Rasulillah. Allahumma-ftah li abwaba rahmatik.",
            translationEn = "In the name of Allah, and peace and blessings be upon the Messenger of Allah. O Allah, open the doors of Your mercy for me.",
            translationSo = "Magaca Eebe, naxariis iyo nabadgelyana Rasuulka Eebbe ha dushiisa ahaato. Allow ii fur albaabbada naxariistaada.",
            translationTr = "Allah'ın adıyla, salât ve selâm Resûlullah'ın üzerine olsun. Allah'ım! Bana rahmetinin kapılarını aç.",
            translationFr = "Au nom d'Allah, et prière et salut sur le Messager d'Allah. Ô Allah ! Ouvre-moi les portes de Ta miséricorde.",
            targetCount = 1,
            reference = "Sahih Muslim 713, Sunan Ibn Majah 771"
        )
    )
}
