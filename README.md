# Parkour Maker

[![Build Status](https://travis-ci.org/noonmaru/parkour-maker.svg?branch=master)](https://travis-ci.org/noonmaru/parkour-maker)
[![JitPack](https://img.shields.io/jitpack/v/github/noonmaru/parkour-maker)](https://jitpack.io/#noonmaru/parkour-maker)
[![License](https://img.shields.io/github/license/noonmaru/parkour-maker)](https://github.com/noonmaru/parkour-maker/blob/master/LICENSE)
[![Twitch Status](https://img.shields.io/twitch/status/hptgrm)](https://twitch.tv/hptgrm)

---

> **Kotlin**으로 작성된 **Paper 1.16.1** 용 파쿠르 제작 플러그인

Parkour Maker는 마인크래프트에서 역동적인 파쿠르 맵을 제작하는데 도움을 주는 플러그인입니다.  

## 시작하기

### PaperMC 서버 설치

Parkour Maker는 PaperMC에서 동작하는 마인크래프트 플러그인으로, PaperMC 서버가 필요합니다.  

[PaperMC 다운로드 문서](https://papermc.io/downloads)에서 PaperMC를 다운로드받습니다.

환경에 맞춰 다운로드받은 PaperMC 파일과 같은 경로에 `.cmd`, `.bat` 또는 `.sh` 파일을 추가합니다.

```bash
java -Xmx<메모리할당량> -jar <파일명> --nogui
```

아래와 같이 입력하면 [Paper 1.16.2의 184번째 빌드 파일](https://github.com/PaperMC/Paper/commit/43e5174a0157c04709f8132ab3711237d76d0954)을 메모리에 1GB만큼 할당하고 서버를 실행할 수 있습니다:

```bash
java -Xmx1G -jar paper-184.jar --nogui
```

* 팁:
```
리눅스 환경에서
nohup sh <file> &&
를 입력하면 터미널이 종료되어도 계속해서 서버를 실행할 수 있습니다.
```

### Parkour Maker 다운로드

Parkour Maker는 개발 중인 상태로, 배포용 릴리즈 판이 없습니다.  
대신 개발 중의 Parkour Maker를 본 리포지토리의 [우측 상단 Code 버튼](https://github.com/noonmaru/parkour-maker/archive/master.zip)을 통해 다운로드 받으실 수 있습니다. (또는 [여기](https://github.com/noonmaru/parkour-maker/archive/master.zip)를 클릭)  

Git을 통해 리포지토리를 클론할 수도 있습니다.  

```bash
git clone -b master https://github.com/noonmaru/parkour-maker.git
```

### 프로젝트 빌드

Parkour Maker는 릴리즈 판이 없으므로 직접 빌드해서 사용해야합니다.  

Parkour Maker의 빌드 설정은 각별 개인의 작업 환경에 맞추어 설정되어있으므로 수정이 필요합니다.  
메모장이나 에디터를 열어 `build.gradle.kts` 파일을 열어 아래 내용을 수정합니다:
```kts
tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
(...)
    create<Copy>("distJar") {
        from(shadowJar)
        into("W:\\Servers\\parkour-maker\\plugins")
    }
}
```
을 아래와 같이 수정합니다.  

```kts
tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
(...)
    create<Copy>("distJar") {
        from(shadowJar)
        into("<경로>")
    }
}
```

수정 후 gradlew를 통해 프로젝트를 빌드합니다.  

**명령 프롬프트**
```bash
cd <경로>
gradlew clean distJar
```

**PowerShell**
```PowerShell
cd <경로>
.\gradlew clean distJar
```

### 의존성 플러그인 설치
Parkour Maker는 몇가지 의존성 플러그인을 서버에 함께 적용해야 작동될 수 있습니다.

* [Tap](https://github.com/noonmaru/tap)
* [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)
* [Kotlin](https://github.com/noonmaru/kotlin-plugin)
* [WorldEdit](https://dev.bukkit.org/projects/worldedit)

[이슈 #2](https://github.com/noonmaru/parkour-maker/issues/2), `파쿠르 메이커 적용 및 작동 방법 문의 #2`에서 작동이 확인된 의존성 플러그인은 아래와 같습니다.

* [Tap 2.3.1](https://github.com/noonmaru/tap/releases/download/2.3.4/tap-2.3.4-dist.jar)
* [ProtocolLib 4.5.0](https://repo.dmulloy2.net/nexus/repository/releases/com/comphenix/protocol/ProtocolLib/4.5.0/ProtocolLib-4.5.0.jar)
* [Kotlin 1.3.70](https://github.com/noonmaru/kotlin-plugin/releases/download/1.3.70/kotlin-1.3.70-lib.jar)
* [WorldEdit 7.1.0](https://media.forgecdn.net/files/2869/453/worldedit-bukkit-7.1.0.jar)

### 인게임
```
/parkour
```

## 기여
Parkour Maker는 완전한 오픈소스 프로젝트입니다. 새로운 기능을 추가하거나 버그를 수정하고 [Pull Request를 생성해보세요.](https://github.com/noonmaru/parkour-maker/compare)

### 이슈 생성
Parkour Maker 프로젝트 사용, 실행 등에 문제가 생겼거나 문의하고자 하는 내용이 있다면, [Issue를 생성하세요.](https://github.com/noonmaru/parkour-maker/issues/new/choose)

### 기여자

* 각별([@noonmaru](https://github.com/noonmaru)) - _프로젝트 시작 및 주요 개발자_
* Patrick([@patrick-mc](https://github.com/patrick-mc)) - _프로젝트 주요 기여자_

Parkour Maker의 전체 기여자 목록은 [Insights](https://github.com/noonmaru/parkour-maker/graphs/contributors)에서 확인하실 수 있습니다.

## 라이선스
Parkour Maker는 [GPL 3.0 License](./LICENSE)(이하 이용허락)의 보호를 받고 있으며, Parkour Maker를 사용하고자 한다면 이용허락을 준수해야 합니다. 이용허락 위반 사례에 대해서 권리자는 위반 관계자에게 법적인 절차를 시행할 수 있으며, 그 책임은 모두 이용허락 위반 사용자에게 있습니다. [자세한 내용은 문서를 참조하세요.](./LICENSE)

## 관련 영상

| 공개 | 업데이트 |
| --- | --- |
| [![ON / OFF - Youtube](https://img.youtube.com/vi/Kfg0RvjHzD0/0.jpg)](https://www.youtube.com/watch?v=Kfg0RvjHzD0) | [![떨어지는 블록 - Youtube](https://img.youtube.com/vi/3nq2CraRkH0/0.jpg)](https://www.youtube.com/watch?v=3nq2CraRkH0) |

